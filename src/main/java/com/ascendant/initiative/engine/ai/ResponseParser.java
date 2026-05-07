package com.ascendant.initiative.engine.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseParser {

    private final ObjectMapper objectMapper;

    public AIScoreResult parse(String rawResponse, int tokensUsed) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return fallback(tokensUsed);
        }

        try {
            // Strip markdown fences if present
            String cleaned = rawResponse
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

            // Find JSON object boundaries
            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start == -1 || end == -1) return fallback(tokensUsed);

            String json = cleaned.substring(start, end + 1);
            AIScoreResult result = objectMapper.readValue(json, AIScoreResult.class);
            result.setTokensUsed(tokensUsed);

            // Clamp values to valid range
            result.setIntellect(clamp(result.getIntellect(), 1, 10));
            result.setJudgment(clamp(result.getJudgment(), 1, 10));
            result.setAwareness(clamp(result.getAwareness(), 1, 10));
            result.setClarity(clamp(result.getClarity(), 1, 10));
            result.setAiScore(Math.max(0.0, Math.min(1.0, result.getAiScore())));

            return result;

        } catch (Exception e) {
            log.error("Failed to parse AI response: {} | Error: {}", rawResponse, e.getMessage());
            return fallback(tokensUsed);
        }
    }

    private AIScoreResult fallback(int tokensUsed) {
        return AIScoreResult.builder()
            .intellect(5).judgment(5).awareness(5).clarity(5)
            .aiScore(0.5)
            .feedback("Good effort! Keep practising your reasoning skills.")
            .tokensUsed(tokensUsed)
            .build();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
