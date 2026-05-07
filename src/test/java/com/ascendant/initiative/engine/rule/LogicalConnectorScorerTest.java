package com.ascendant.initiative.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogicalConnectorScorerTest {

    private LogicalConnectorScorer scorer;

    @BeforeEach void setUp() { scorer = new LogicalConnectorScorer(); }

    @Test @DisplayName("No connectors returns 0.0")
    void noConnectors() {
        assertThat(scorer.score("I like this idea very much")).isEqualTo(0.0);
    }

    @Test @DisplayName("Null returns 0.0")
    void nullText() {
        assertThat(scorer.score(null)).isEqualTo(0.0);
    }

    @Test @DisplayName("Contrast connector alone gives partial score")
    void contrastAlone() {
        double score = scorer.score("I like it however I am not sure about the result");
        assertThat(score).isGreaterThan(0.0).isLessThan(1.0);
    }

    @Test @DisplayName("If-then pair gives causation score")
    void ifThenPair() {
        double score = scorer.score("If it rains today then we should cancel the event completely");
        assertThat(score).isGreaterThan(0.0);
    }

    @Test @DisplayName("Both contrast and if-then returns high score")
    void bothConnectors() {
        double score = scorer.score(
                "If the price is too high then fewer people will buy it. " +
                "However we also need to cover our costs. " +
                "Although this seems difficult we can find a balance.");
        assertThat(score).isGreaterThanOrEqualTo(0.5);
    }

    @Test @DisplayName("Score capped at 1.0")
    void cappedAtOne() {
        String text = "if a then b. if c then d. however although but nevertheless despite " +
                      "whereas if e then f. if g then h.";
        assertThat(scorer.score(text)).isLessThanOrEqualTo(1.0);
    }
}
