package com.ascendant.initiative.service;

import com.ascendant.initiative.dto.evaluation.EvaluationResultDto;
import com.ascendant.initiative.dto.response.ResponseSubmitDto;
import com.ascendant.initiative.dto.response.ResponseSubmitRequest;
import com.ascendant.initiative.engine.ai.AIScoreResult;
import com.ascendant.initiative.engine.ai.CostController;
import com.ascendant.initiative.engine.ai.LLMClient;
import com.ascendant.initiative.engine.ai.PromptBuilder;
import com.ascendant.initiative.engine.ai.ResponseParser;
import com.ascendant.initiative.engine.rule.RuleEngine;
import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.*;
import com.ascendant.initiative.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    private final ResponseRepository responseRepository;
    private final EvaluationRepository evaluationRepository;
    private final MissionRepository missionRepository;
    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;
    private final ProgressionLogRepository progressionLogRepository;
    private final RuleEngine ruleEngine;
    private final PromptBuilder promptBuilder;
    private final LLMClient llmClient;
    private final ResponseParser responseParser;
    private final CostController costController;
    private final ProgressService progressService;

    /**
     * Submit a response. Stores it immediately and triggers async evaluation.
     * Returns PENDING status right away — UI polls /evaluation endpoint.
     */
    @Transactional
    public ResponseSubmitDto submit(ResponseSubmitRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> AppException.notFound("User not found"));
                
        if (user.getRole() == User.Role.CHILD) {
            ParentChildLink link = parentChildLinkRepository.findByChildId(user.getId())
                    .orElseThrow(() -> AppException.forbidden("Parent approval required to play missions. No parent linked."));
            if (!link.getApproved()) {
                throw AppException.forbidden("Parent approval required to play missions. Account is pending.");
            }
        }

        Mission mission = missionRepository.findById(req.getMissionId())
                .orElseThrow(() -> AppException.notFound("Mission not found"));
        Scenario scenario = scenarioRepository.findById(req.getScenarioId())
                .orElseThrow(() -> AppException.notFound("Scenario not found"));

        // Count words
        int wordCount = req.getAnswerText().trim().split("\\s+").length;

        Response response = Response.builder()
                .user(user)
                .mission(mission)
                .scenario(scenario)
                .answerText(req.getAnswerText())
                .selectedChoice(req.getSelectedChoice())
                .wordCount(wordCount)
                .build();
        responseRepository.save(response);

        // Create PENDING evaluation record
        Evaluation evaluation = Evaluation.builder()
                .response(response)
                .evalStatus(Evaluation.EvalStatus.PENDING)
                .build();
        evaluationRepository.save(evaluation);

        // Check AI quota
        boolean hasAiQuota = costController.hasRemainingQuota(user);

        // Fire async evaluation
        evaluateAsync(response.getId(), hasAiQuota);

        return ResponseSubmitDto.builder()
                .responseId(response.getId())
                .status("PENDING")
                .message("Evaluation in progress...")
                .estimatedSeconds(hasAiQuota ? 5 : 2)
                .aiLimited(!hasAiQuota)
                .build();
    }

    /**
     * Async evaluation: runs Rule Engine and optionally AI Engine in parallel,
     * merges scores using mission-specific weights, saves result.
     */
    @Async("evaluationExecutor")
    public void evaluateAsync(UUID responseId, boolean useAi) {
        try {
            Response response = responseRepository.findById(responseId)
                    .orElseThrow(() -> new RuntimeException("Response not found: " + responseId));
            Mission mission = response.getMission();
            User user = response.getUser();

            // ── Rule Engine (sync, fast) ─────────────────────────────────
            var ruleResult = ruleEngine.score(response, mission);

            // ── AI Engine (async, slower) ────────────────────────────────
            AIScoreResult aiResult = null;
            if (useAi) {
                String prompt = promptBuilder.buildEvaluationPrompt(response, mission);
                LLMClient.LLMResponse llmResponse = llmClient.call(prompt);

                if (llmResponse.success()) {
                    aiResult = responseParser.parse(llmResponse.content(), llmResponse.tokensUsed());
                    costController.logUsage(user, llmResponse.tokensUsed());
                }
            }

            // ── Dynamic Score Merge ──────────────────────────────────────
            double ruleWeight = mission.getRuleWeight();
            double aiWeight   = mission.getAiWeight();
            double finalScore;
            Double intellectScore = null, judgmentScore = null, awarenessScore = null, clarityScore = null;
            String feedbackText = null;
            int tokensUsed = 0;

            if (aiResult != null) {
                finalScore     = (ruleResult.finalScore() * ruleWeight) + (aiResult.getAiScore() * aiWeight);
                intellectScore = aiResult.getIntellectNormalized();
                judgmentScore  = aiResult.getJudgmentNormalized();
                awarenessScore = aiResult.getAwarenessNormalized();
                clarityScore   = aiResult.getClarityNormalized();
                feedbackText   = aiResult.getFeedback();
                tokensUsed     = aiResult.getTokensUsed();
            } else {
                // Rule-only fallback
                finalScore = ruleResult.finalScore();
                feedbackText = "Good effort! Your answer showed some strong thinking. Keep going!";
            }

            // ── Persist Evaluation ───────────────────────────────────────
            Evaluation evaluation = evaluationRepository.findByResponseId(responseId)
                    .orElseThrow(() -> new RuntimeException("Evaluation record missing"));

            evaluation.setRuleScore(ruleResult.finalScore());
            evaluation.setAiScore(aiResult != null ? aiResult.getAiScore() : null);
            evaluation.setFinalScore(finalScore);
            evaluation.setIntellectScore(intellectScore);
            evaluation.setJudgmentScore(judgmentScore);
            evaluation.setAwarenessScore(awarenessScore);
            evaluation.setClarityScore(clarityScore);
            evaluation.setFeedbackText(feedbackText);
            evaluation.setAiTokensUsed(tokensUsed);
            evaluation.setEvalStatus(Evaluation.EvalStatus.DONE);
            evaluationRepository.save(evaluation);

            // ── Update Progression ───────────────────────────────────────
            progressService.record(user, mission, evaluation);

            log.info("Evaluation complete for response {} | finalScore={} | ruleScore={} | aiScore={}",
                    responseId, String.format("%.3f", finalScore),
                    String.format("%.3f", ruleResult.finalScore()),
                    aiResult != null ? String.format("%.3f", aiResult.getAiScore()) : "N/A");

        } catch (Exception e) {
            log.error("Evaluation FAILED for response {}: {}", responseId, e.getMessage(), e);
            evaluationRepository.findByResponseId(responseId).ifPresent(eval -> {
                eval.setEvalStatus(Evaluation.EvalStatus.FAILED);
                evaluationRepository.save(eval);
            });
        }
    }

    /**
     * Poll endpoint — returns current evaluation state.
     */
    @Transactional(readOnly = true)
    public EvaluationResultDto getResult(UUID responseId) {
        Response response = responseRepository.findById(responseId)
                .orElseThrow(() -> AppException.notFound("Response not found: " + responseId));

        Evaluation eval = evaluationRepository.findByResponseId(responseId)
                .orElseThrow(() -> AppException.notFound("Evaluation not found"));

        EvaluationResultDto.EvaluationDto evalDto = null;
        Integer xpEarned = null;
        Boolean leveledUp = null;
        Integer newLevel = null;

        if (eval.getEvalStatus() == Evaluation.EvalStatus.DONE) {
            evalDto = EvaluationResultDto.EvaluationDto.builder()
                    .id(eval.getId())
                    .ruleScore(eval.getRuleScore())
                    .aiScore(eval.getAiScore())
                    .finalScore(eval.getFinalScore())
                    .intellectScore(eval.getIntellectScore())
                    .judgmentScore(eval.getJudgmentScore())
                    .awarenessScore(eval.getAwarenessScore())
                    .clarityScore(eval.getClarityScore())
                    .feedbackText(eval.getFeedbackText())
                    .aiTokensUsed(eval.getAiTokensUsed())
                    .evalStatus(eval.getEvalStatus())
                    .createdAt(eval.getCreatedAt())
                    .build();

            progressionLogRepository.findByResponseId(responseId).ifPresent(log -> {
                // Populate XP and level data from the actual progression log
            });
            // Java 11/17 lambda workaround
            ProgressionLog pLog = progressionLogRepository.findByResponseId(responseId).orElse(null);
            if (pLog != null) {
                xpEarned = pLog.getXpEarned();
                leveledUp = pLog.getLeveledUp();
                newLevel = pLog.getLevelAfter();
            }
        }

        return EvaluationResultDto.builder()
                .responseId(responseId)
                .status(eval.getEvalStatus())
                .evaluation(evalDto)
                .xpEarned(xpEarned)
                .leveledUp(leveledUp)
                .newLevel(newLevel)
                .answerText(response.getAnswerText())
                .selectedChoice(response.getSelectedChoice())
                .build();
    }
}
