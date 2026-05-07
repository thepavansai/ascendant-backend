package com.ascendant.initiative.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HypothesisDetectorTest {

    private HypothesisDetector detector;

    @BeforeEach void setUp() { detector = new HypothesisDetector(); }

    @Test @DisplayName("If-then pattern returns 1.0")
    void ifThenFull() {
        assertThat(detector.score("If the price drops then more people will buy it"))
                .isEqualTo(1.0);
    }

    @Test @DisplayName("Question mark gives 0.5")
    void questionMark() {
        assertThat(detector.score("What will happen if we wait longer?"))
                .isEqualTo(0.5);
    }

    @Test @DisplayName("What-if phrase gives 0.8")
    void whatIf() {
        assertThat(detector.score("What if we change the price to be lower for everyone"))
                .isEqualTo(0.8);
    }

    @Test @DisplayName("No hypothesis markers returns 0.0")
    void noHypothesis() {
        assertThat(detector.score("I think this is a good plan and it will work"))
                .isEqualTo(0.0);
    }

    @Test @DisplayName("Null returns 0.0")
    void nullText() {
        assertThat(detector.score(null)).isEqualTo(0.0);
    }
}
