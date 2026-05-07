package com.ascendant.initiative.engine.ai;

import com.ascendant.initiative.model.Mission;
import com.ascendant.initiative.model.Response;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildEvaluationPrompt(Response response, Mission mission) {
        return """
            You are evaluating a child's (age 9-12) written response to a thinking mission.
            Be fair, encouraging, and age-appropriate in your feedback.

            Mission Title: %s
            Mission Type: %s
            Difficulty Level: %d / 5
            Scenario Context: %s

            Child's Response:
            "%s"

            Evaluate this response on exactly 4 dimensions (score each 1–10):
            - intellect: Quality of logical reasoning, analysis, and use of evidence
            - judgment: Quality of decision-making and awareness of trade-offs
            - awareness: Identification of the core problem and its broader implications
            - clarity: How clearly and simply the idea is communicated

            Return ONLY a valid JSON object. No markdown. No explanation outside the JSON.
            {
              "intellect": <integer 1-10>,
              "judgment": <integer 1-10>,
              "awareness": <integer 1-10>,
              "clarity": <integer 1-10>,
              "feedback": "<2-3 sentences. Start positive. Name one strength. Name one thing to improve next time. Keep language simple for a child.>",
              "ai_score": <float between 0.0 and 1.0 representing overall quality>
            }
            """.formatted(
                mission.getTitle(),
                mission.getMissionType().name(),
                mission.getDifficultyLevel(),
                mission.getScenario() != null ? mission.getScenario().getContext() : "No specific scenario",
                response.getAnswerText()
            );
    }
}
