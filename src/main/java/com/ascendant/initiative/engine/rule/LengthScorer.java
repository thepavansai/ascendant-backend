package com.ascendant.initiative.engine.rule;

import org.springframework.stereotype.Component;

@Component
public class LengthScorer {

    // Minimum words for full score by difficulty
    private static final int[] FULL_SCORE_WORDS  = {0, 30, 50, 80, 110, 150};
    private static final int[] ZERO_SCORE_WORDS  = {0, 10, 15, 20, 30,  40};

    public double score(String text, int difficulty) {
        int wordCount = countWords(text);
        int fullScore = FULL_SCORE_WORDS[Math.min(difficulty, 5)];
        int zeroScore = ZERO_SCORE_WORDS[Math.min(difficulty, 5)];

        if (wordCount >= fullScore) return 1.0;
        if (wordCount <= zeroScore) return 0.0;

        return (double)(wordCount - zeroScore) / (fullScore - zeroScore);
    }

    public int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
