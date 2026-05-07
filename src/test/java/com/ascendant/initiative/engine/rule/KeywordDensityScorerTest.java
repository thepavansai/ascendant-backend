package com.ascendant.initiative.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordDensityScorerTest {

    private KeywordDensityScorer scorer;

    @BeforeEach void setUp() { scorer = new KeywordDensityScorer(); }

    @Test @DisplayName("No keywords returns 0.0")
    void noKeywords() {
        assertThat(scorer.score("I like cats and dogs very much", 2)).isEqualTo(0.0);
    }

    @Test @DisplayName("Null/blank returns 0.0")
    void nullText() {
        assertThat(scorer.score(null, 2)).isEqualTo(0.0);
        assertThat(scorer.score("   ", 2)).isEqualTo(0.0);
    }

    @Test @DisplayName("High density returns 1.0 (capped)")
    void highDensityCapped() {
        String text = "because therefore since because therefore since because " +
                      "therefore since because therefore since because ";
        assertThat(scorer.score(text, 1)).isEqualTo(1.0);
    }

    @Test @DisplayName("Single keyword in long text gives low score")
    void singleKeywordLowDensity() {
        String text = "word ".repeat(100) + "because";
        double score = scorer.score(text, 2);
        assertThat(score).isGreaterThan(0.0).isLessThan(0.4);
    }

    @Test @DisplayName("Score never exceeds 1.0")
    void scoreNeverExceedsOne() {
        String text = "because therefore since because therefore since " +
                      "because therefore since because ";
        assertThat(scorer.score(text, 1)).isLessThanOrEqualTo(1.0);
    }
}
