package com.ascendant.initiative.engine.rule;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogicalConnectorScorer {

    private static final List<String> CONTRAST_CONNECTORS = List.of(
        "however", "but", "although", "even though", "on the other hand",
        "despite", "nevertheless", "yet", "whereas", "while"
    );

    private static final Pattern IF_THEN_PATTERN =
        Pattern.compile("if\\s+.{3,80}?\\s+then\\s+.{3,}", Pattern.CASE_INSENSITIVE);

    public double score(String text) {
        if (text == null || text.isBlank()) return 0.0;
        String lowerText = text.toLowerCase();

        double contrastScore = scoreContrast(lowerText);
        double causationScore = scoreCausation(lowerText);

        // Weighted: causation matters more than contrast
        double combined = (contrastScore * 0.4) + (causationScore * 0.6);
        return Math.min(combined, 1.0);
    }

    private double scoreContrast(String text) {
        long found = CONTRAST_CONNECTORS.stream()
            .filter(c -> Pattern.compile("\\b" + Pattern.quote(c) + "\\b").matcher(text).find())
            .count();
        return found >= 2 ? 1.0 : found == 1 ? 0.5 : 0.0;
    }

    private double scoreCausation(String text) {
        Matcher m = IF_THEN_PATTERN.matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count >= 2 ? 1.0 : count == 1 ? 0.7 : 0.0;
    }
}
