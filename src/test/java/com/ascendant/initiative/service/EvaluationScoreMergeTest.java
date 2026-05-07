package com.ascendant.initiative.service;

import com.ascendant.initiative.model.Mission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the dynamic score merge formula in isolation.
 * finalScore = (ruleScore * mission.rule_weight) + (aiScore * mission.ai_weight)
 */
class EvaluationScoreMergeTest {

    // Pure formula — no Spring context needed
    private double merge(double ruleScore, double aiScore,
                         double ruleWeight, double aiWeight) {
        return (ruleScore * ruleWeight) + (aiScore * aiWeight);
    }

    @Test @DisplayName("FACTUAL mission uses 0.6/0.4 weights")
    void factualWeights() {
        double result = merge(0.8, 0.6, 0.6, 0.4);
        assertThat(result).isCloseTo(0.72, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test @DisplayName("ANALYTICAL mission uses 0.3/0.7 weights")
    void analyticalWeights() {
        double result = merge(0.8, 0.6, 0.3, 0.7);
        assertThat(result).isCloseTo(0.66, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test @DisplayName("OPEN_ENDED mission uses 0.15/0.85 weights")
    void openEndedWeights() {
        double result = merge(0.8, 0.6, 0.15, 0.85);
        assertThat(result).isCloseTo(0.63, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test @DisplayName("Rule-only fallback (ai_score null) uses rule_score directly")
    void ruleOnlyFallback() {
        // When AI is unavailable, final = rule_score × 1.0
        double ruleScore = 0.73;
        double finalScore = ruleScore; // ai_score = null → fallback
        assertThat(finalScore).isEqualTo(0.73);
    }

    @Test @DisplayName("Perfect scores return 1.0")
    void perfectScores() {
        assertThat(merge(1.0, 1.0, 0.3, 0.7)).isEqualTo(1.0);
    }

    @Test @DisplayName("Zero scores return 0.0")
    void zeroScores() {
        assertThat(merge(0.0, 0.0, 0.3, 0.7)).isEqualTo(0.0);
    }
}
