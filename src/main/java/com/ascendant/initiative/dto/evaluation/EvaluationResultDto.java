package com.ascendant.initiative.dto.evaluation;

import com.ascendant.initiative.model.Evaluation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class EvaluationResultDto {
    private UUID responseId;
    private Evaluation.EvalStatus status;
    private EvaluationDto evaluation;
    private Integer xpEarned;
    private Boolean leveledUp;
    private Integer newLevel;
    private String answerText;
    private String selectedChoice;

    @Data @Builder
    public static class EvaluationDto {
        private UUID id;
        private Double ruleScore;
        private Double aiScore;
        private Double finalScore;
        private Double intellectScore;
        private Double judgmentScore;
        private Double awarenessScore;
        private Double clarityScore;
        private String feedbackText;
        private Integer aiTokensUsed;
        private Evaluation.EvalStatus evalStatus;
        private LocalDateTime createdAt;
    }
}
