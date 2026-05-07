package com.ascendant.initiative.engine.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIScoreResult {
    private int intellect;
    private int judgment;
    private int awareness;
    private int clarity;
    private String feedback;

    @JsonProperty("ai_score")
    private double aiScore;

    private int tokensUsed;

    // Normalized scores (0.0–1.0)
    public double getIntellectNormalized() { return intellect / 10.0; }
    public double getJudgmentNormalized()  { return judgment / 10.0;  }
    public double getAwarenessNormalized() { return awareness / 10.0; }
    public double getClarityNormalized()   { return clarity / 10.0;   }
}
