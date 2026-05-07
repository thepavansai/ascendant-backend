package com.ascendant.initiative.engine.rule;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class HypothesisDetector {

    private static final Pattern IF_THEN = Pattern.compile(
        "if\\s+.{3,80}?\\s+then\\s+.{3,}",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern QUESTION = Pattern.compile("\\?");
    private static final Pattern WHAT_IF = Pattern.compile(
        "what if|suppose|imagine if|let's say",
        Pattern.CASE_INSENSITIVE
    );

    public double score(String text) {
        if (text == null || text.isBlank()) return 0.0;

        boolean hasIfThen = IF_THEN.matcher(text).find();
        boolean hasQuestion = QUESTION.matcher(text).find();
        boolean hasWhatIf = WHAT_IF.matcher(text).find();

        if (hasIfThen) return 1.0;
        if (hasWhatIf) return 0.8;
        if (hasQuestion) return 0.5;
        return 0.0;
    }
}
