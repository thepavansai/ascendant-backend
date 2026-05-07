package com.ascendant.initiative.service;

import com.ascendant.initiative.util.XpCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XpCalculatorTest {

    private XpCalculator calc;

    @BeforeEach void setUp() { calc = new XpCalculator(); }

    @Test @DisplayName("Difficulty 2, score 1.0 → 100 XP")
    void difficulty2Perfect() {
        assertThat(calc.calculateXpEarned(1.0, 2)).isEqualTo(100);
    }

    @Test @DisplayName("Difficulty 1, score 1.0 → 50 XP (0.5 multiplier)")
    void difficulty1Perfect() {
        assertThat(calc.calculateXpEarned(1.0, 1)).isEqualTo(50);
    }

    @Test @DisplayName("Difficulty 5, score 1.0 → 300 XP (3.0 multiplier)")
    void difficulty5Perfect() {
        assertThat(calc.calculateXpEarned(1.0, 5)).isEqualTo(300);
    }

    @Test @DisplayName("Difficulty 2, score 0.5 → 50 XP")
    void halfScore() {
        assertThat(calc.calculateXpEarned(0.5, 2)).isEqualTo(50);
    }

    @Test @DisplayName("Zero score returns 0 XP")
    void zeroScore() {
        assertThat(calc.calculateXpEarned(0.0, 3)).isEqualTo(0);
    }

    @Test @DisplayName("Level calculation — 0 XP = Level 1")
    void levelZeroXp() {
        assertThat(calc.calculateLevel(0)).isEqualTo(1);
    }

    @Test @DisplayName("Level calculation — 200 XP = Level 2")
    void level2() {
        assertThat(calc.calculateLevel(200)).isEqualTo(2);
    }

    @Test @DisplayName("Level calculation — 1000 XP = Level 4")
    void level4() {
        assertThat(calc.calculateLevel(1000)).isEqualTo(4);
    }

    @Test @DisplayName("XP to next level — at 0 XP need 200 for level 2")
    void xpToNextAtZero() {
        assertThat(calc.xpToNextLevel(0)).isEqualTo(200);
    }

    @Test @DisplayName("XP to next level — at 200 XP need 300 for level 3")
    void xpToNextAtLevel2() {
        assertThat(calc.xpToNextLevel(200)).isEqualTo(300);
    }
}
