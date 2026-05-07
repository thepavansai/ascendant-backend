package com.ascendant.initiative.engine.rule;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class KeywordDensityScorer {

    private static final List<String> REASONING_KEYWORDS = List.of(
        "because", "therefore", "since", "due to", "as a result",
        "which means", "consequently", "hence", "so that", "in order to",
        "this leads to", "this causes", "this results in"
    );

    // Expected keyword density: keywords per 50 words by difficulty
    private static final double[] EXPECTED_DENSITY = {0, 1.0, 1.5, 2.0, 2.5, 3.0};

    public double score(String text, int difficulty) {
        if (text == null || text.isBlank()) return 0.0;

        String lowerText = text.toLowerCase();
        int wordCount = text.trim().split("\\s+").length;
        if (wordCount == 0) return 0.0;

        long keywordCount = REASONING_KEYWORDS.stream()
            .filter(kw -> containsKeyword(lowerText, kw))
            .count();

        // Density = keywords per 50 words
        double density = (keywordCount * 50.0) / wordCount;
        double expected = EXPECTED_DENSITY[Math.min(difficulty, 5)];

        return Math.min(density / expected, 1.0);
    }

    private boolean containsKeyword(String text, String keyword) {
        return Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b")
            .matcher(text).find();
    }
}
