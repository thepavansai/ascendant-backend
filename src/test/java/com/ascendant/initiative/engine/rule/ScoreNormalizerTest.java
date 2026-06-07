package com.ascendant.initiative.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreNormalizerTest {

    private ScoreNormalizer normalizer;

    @BeforeEach void setUp() { normalizer = new ScoreNormalizer(); }

    @Test @DisplayName("All ones returns 1.0")
    void allOnes() {
        assertThat(normalizer.normalize(1.0, 1.0, 1.0, 1.0)).isEqualTo(1.0);
    }

    @Test @DisplayName("All zeros returns 0.0")
    void allZeros() {
        assertThat(normalizer.normalize(0.0, 0.0, 0.0, 0.0)).isEqualTo(0.0);
    }

    @Test @DisplayName("Result always in [0.0, 1.0]")
    void alwaysInBounds() {
        assertThat(normalizer.normalize(0.5, 0.8, 0.3, 0.9))
                .isGreaterThanOrEqualTo(0.0)
                .isLessThanOrEqualTo(1.0);
    }

    @Test @DisplayName("Weights sum check — partial scores give partial result")
    void partialScores() {
        // All 0.5 → result should be 0.5
        double result = normalizer.normalize(0.5, 0.5, 0.5, 0.5);
        assertThat(result).isCloseTo(0.5, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test @DisplayName("Negative inputs clamped to 0.0")
    void negativeClamp() {
        assertThat(normalizer.normalize(-1.0, -1.0, -1.0, -1.0)).isEqualTo(0.0);
    }

    @Test @DisplayName("Values greater than 1.0 are clamped to 1.0")
    void upperClamp() {
        assertThat(normalizer.normalize(1.5, 2.0, 1.1, 5.0)).isEqualTo(1.0);
    }
}
