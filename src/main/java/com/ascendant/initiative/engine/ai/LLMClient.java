package com.ascendant.initiative.engine.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LLMClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final int maxOutputTokens;
    private final double temperature;

    /**
     * All config is read from application.yml — nothing hardcoded.
     * Supports both Anthropic and OpenAI-compatible providers (Groq, etc.)
     * based on the api-url value.
     */
    public LLMClient(
            ObjectMapper objectMapper,
            @Value("${app.ai.claude.api-url:https://api.anthropic.com/v1/messages}") String apiUrl,
            @Value("${app.ai.claude.api-key:}") String apiKey,
            @Value("${app.ai.claude.model:claude-sonnet-4-20250514}") String model,
            @Value("${app.ai.claude.max-output-tokens:300}") int maxOutputTokens,
            @Value("${app.ai.claude.temperature:1}") double temperature) {

        this.objectMapper    = objectMapper;
        this.apiUrl          = apiUrl;
        this.apiKey          = apiKey;
        this.model           = model;
        this.maxOutputTokens = maxOutputTokens;
        this.temperature     = temperature;

        this.webClient = WebClient.builder()
                .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("LLMClient initialised → url={} model={}", apiUrl, model);
    }

    public LLMResponse call(String prompt) {
        boolean isOpenAiCompatible = isOpenAiCompatible(apiUrl);

        // Build request body based on provider format
        Map<String, Object> requestBody;
        if (isOpenAiCompatible) {
            requestBody = Map.of(
                    "model",                model,
                    "max_completion_tokens", maxOutputTokens,
                    "temperature",          temperature,
                    "top_p",                1,
                    "stream",               false,
                    "reasoning_effort",     "medium",
                    "messages",             List.of(Map.of("role", "user", "content", prompt))
            );
        } else {
            // Anthropic format
            requestBody = Map.of(
                    "model",      model,
                    "max_tokens", maxOutputTokens,
                    "messages",   List.of(Map.of("role", "user", "content", prompt))
            );
        }

        try {
            // Build request — auth header differs between Anthropic and OpenAI-compatible
            var requestSpec = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(requestBody);

            if (isOpenAiCompatible) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + apiKey);
            } else {
                requestSpec = requestSpec
                        .header("x-api-key", apiKey)
                        .header("anthropic-version", "2023-06-01");
            }

            String responseJson = requestSpec
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);

            // Parse response — format differs between Anthropic and OpenAI-compatible
            String content;
            int inputTokens, outputTokens;

            if (isOpenAiCompatible) {
                // OpenAI format: choices[0].message.content
                content      = root.path("choices").get(0).path("message").path("content").asText();
                inputTokens  = root.path("usage").path("prompt_tokens").asInt(0);
                outputTokens = root.path("usage").path("completion_tokens").asInt(0);
            } else {
                // Anthropic format: content[0].text
                content      = root.path("content").get(0).path("text").asText();
                inputTokens  = root.path("usage").path("input_tokens").asInt(0);
                outputTokens = root.path("usage").path("output_tokens").asInt(0);
            }

            return new LLMResponse(content, inputTokens + outputTokens, true);

        } catch (Exception e) {
            log.error("LLM API call failed [url={}]: {}", apiUrl, e.getMessage());
            return new LLMResponse(null, 0, false);
        }
    }

    /**
     * Detects OpenAI-compatible providers by URL.
     * Anthropic uses x-api-key + its own response format.
     * Groq, OpenAI, Together, etc. use Bearer token + OpenAI response format.
     */
    private boolean isOpenAiCompatible(String url) {
        return url.contains("groq.com")
                || url.contains("openai.com")
                || url.contains("together.ai")
                || url.contains("openrouter.ai");
    }

    public record LLMResponse(String content, int tokensUsed, boolean success) {}
}