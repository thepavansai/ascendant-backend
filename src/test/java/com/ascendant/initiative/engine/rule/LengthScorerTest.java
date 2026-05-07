package com.ascendant.initiative.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LengthScorerTest {

    private LengthScorer scorer;

    @BeforeEach void setUp() { scorer = new LengthScorer(); }

    @Test @DisplayName("Empty text returns 0.0")
    void emptyText() {
        assertThat(scorer.score("", 2)).isEqualTo(0.0);
        assertThat(scorer.score(null, 2)).isEqualTo(0.0);
    }

    @Test @DisplayName("Difficulty 1 — 30+ words returns 1.0")
    void difficulty1FullScore() {
        String text = "word ".repeat(31);
        assertThat(scorer.score(text, 1)).isEqualTo(1.0);
    }

    @Test @DisplayName("Difficulty 1 — under 10 words returns 0.0")
    void difficulty1ZeroScore() {
        assertThat(scorer.score("only five words here", 1)).isEqualTo(0.0);
    }

    @Test @DisplayName("Difficulty 3 — 80+ words returns 1.0")
    void difficulty3FullScore() {
        String text = "word ".repeat(85);
        assertThat(scorer.score(text, 3)).isEqualTo(1.0);
    }

    @Test @DisplayName("Difficulty 5 — 150+ words returns 1.0")
    void difficulty5FullScore() {
        String text = "word ".repeat(155);
        assertThat(scorer.score(text, 5)).isEqualTo(1.0);
    }

    @Test @DisplayName("Proportional score between min and max")
    void proportionalScore() {
        // difficulty 1: zero=10, full=30 → 20 words = 0.5
        String text = "word ".repeat(20);
        double score = scorer.score(text, 1);
        assertThat(score).isGreaterThan(0.0).isLessThan(1.0);
    }

    @Test @DisplayName("Score always in [0.0, 1.0]")
    void scoreBoundaries() {
        for (int d = 1; d <= 5; d++) {
            assertThat(scorer.score("word ".repeat(200), d)).isLessThanOrEqualTo(1.0);
            assertThat(scorer.score("hi", d)).isGreaterThanOrEqualTo(0.0);
        }
    }
}
