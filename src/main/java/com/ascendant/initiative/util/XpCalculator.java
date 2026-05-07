package com.ascendant.initiative.util;

import org.springframework.stereotype.Component;

@Component
public class XpCalculator {

    // Base XP per mission
    private static final int BASE_XP = 100;

    // Difficulty multipliers
    private static final double[] DIFFICULTY_MULTIPLIER = {0, 0.5, 1.0, 1.5, 2.0, 3.0};

    // Level thresholds (logarithmic curve)
    private static final int[] LEVEL_XP = {
        0, 200, 500, 1000, 1800, 2800, 4000, 5500, 7200, 9000, Integer.MAX_VALUE
    };

    public int calculateXpEarned(double finalScore, int difficulty) {
        double multiplier = DIFFICULTY_MULTIPLIER[Math.min(difficulty, 5)];
        return (int) Math.round(BASE_XP * finalScore * multiplier);
    }

    public int calculateLevel(int totalXp) {
        for (int i = LEVEL_XP.length - 1; i >= 0; i--) {
            if (totalXp >= LEVEL_XP[i]) return i + 1;
        }
        return 1;
    }

    public int xpToNextLevel(int totalXp) {
        int currentLevel = calculateLevel(totalXp);
        if (currentLevel >= LEVEL_XP.length) return 0;
        return LEVEL_XP[currentLevel] - totalXp;
    }
}
