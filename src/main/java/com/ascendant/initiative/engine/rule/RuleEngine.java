package com.ascendant.initiative.engine.rule;

import com.ascendant.initiative.model.Mission;
import com.ascendant.initiative.model.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    private final LengthScorer lengthScorer;
    private final KeywordDensityScorer keywordDensityScorer;
    private final LogicalConnectorScorer logicalConnectorScorer;
    private final HypothesisDetector hypothesisDetector;
    private final ScoreNormalizer scoreNormalizer;

    public RuleScoreResult score(Response response, Mission mission) {
        long start = System.currentTimeMillis();

        String text = response.getAnswerText();
        int difficulty = mission.getDifficultyLevel();

        double lengthScore    = lengthScorer.score(text, difficulty);
        double densityScore   = keywordDensityScorer.score(text, difficulty);
        double connectorScore = logicalConnectorScorer.score(text);
        double hypothesisScore = hypothesisDetector.score(text);
        double finalScore     = scoreNormalizer.normalize(lengthScore, densityScore, connectorScore, hypothesisScore);

        long elapsed = System.currentTimeMillis() - start;
        log.debug("Rule Engine scored response {} in {}ms: {}", response.getId(), elapsed, finalScore);

        return new RuleScoreResult(lengthScore, densityScore, connectorScore, hypothesisScore, finalScore);
    }

    public record RuleScoreResult(
        double lengthScore,
        double densityScore,
        double connectorScore,
        double hypothesisScore,
        double finalScore
    ) {}
}
