package com.aquaai.recommendation.infrastructure.ai;

import com.aquaai.ai.infrastructure.AiProperties;
import com.aquaai.recommendation.application.service.AgronomicPromptBuilder;
import com.aquaai.recommendation.domain.model.AIRecommendationResponse;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.port.AgronomicAIProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gemini advisory LLM adapter communicating with Google Generative AI API (gemini-1.5-flash).
 * Enforces structured JSON output and provides graceful error handling.
 */
@Slf4j
@Component
public class GeminiAgronomicAIAdapter implements AgronomicAIProvider {

    private final AiProperties aiProperties;
    private final AgronomicPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiAgronomicAIAdapter(
            AiProperties aiProperties,
            AgronomicPromptBuilder promptBuilder,
            ObjectMapper objectMapper
    ) {
        this.aiProperties = aiProperties;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Optional<AIRecommendationResponse> generateRecommendation(AgronomicRecommendationContext context) {
        if (!isAvailable()) {
            log.debug("Gemini AI provider is not available or API key is not configured.");
            return Optional.empty();
        }

        try {
            String prompt = promptBuilder.buildPrompt(context);
            String apiKey = aiProperties.getGemini().getApiKey();
            String model = aiProperties.getGemini().getModel();

            String endpoint = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    model, apiKey
            );

            // Construct Gemini request payload
            String requestPayload = objectMapper.writeValueAsString(new GeminiRequest(
                    List.of(new Content("user", List.of(new Part(prompt)))),
                    new GenerationConfig(0.2, "application/json")
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Gemini API call failed with status code {}: {}", response.statusCode(), response.body());
                return Optional.empty();
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                log.warn("Gemini API response did not contain candidates content text");
                return Optional.empty();
            }

            String generatedJson = textNode.asText().trim();
            // In case model wraps in markdown fences despite instructions
            if (generatedJson.startsWith("```json")) {
                generatedJson = generatedJson.substring(7);
            }
            if (generatedJson.startsWith("```")) {
                generatedJson = generatedJson.substring(3);
            }
            if (generatedJson.endsWith("```")) {
                generatedJson = generatedJson.substring(0, generatedJson.length() - 3);
            }
            generatedJson = generatedJson.trim();

            JsonNode parsed = objectMapper.readTree(generatedJson);

            RecommendationType recType;
            try {
                recType = RecommendationType.valueOf(parsed.path("recommendationType").asText("MONITOR_SOIL_MOISTURE"));
            } catch (Exception e) {
                recType = RecommendationType.MONITOR_SOIL_MOISTURE;
            }

            RecommendationPriority priority;
            try {
                priority = RecommendationPriority.valueOf(parsed.path("priority").asText("MEDIUM"));
            } catch (Exception e) {
                priority = RecommendationPriority.MEDIUM;
            }

            String title = parsed.path("title").asText("Agronomic Recommendation");
            String summary = parsed.path("summary").asText("Advisory update based on field telemetry.");
            String explanation = parsed.path("explanation").asText("Sensor metrics indicate standard field monitoring.");
            String limitations = parsed.path("limitations").asText("Advisory recommendation generated by Gemini AI.");
            double conf = parsed.path("confidenceScore").asDouble(0.80);

            List<String> actions = new ArrayList<>();
            JsonNode actionsNode = parsed.path("actions");
            if (actionsNode.isArray()) {
                actionsNode.forEach(a -> actions.add(a.asText()));
            }

            List<String> doNotActions = new ArrayList<>();
            JsonNode doNotNode = parsed.path("doNotActions");
            if (doNotNode.isArray()) {
                doNotNode.forEach(a -> doNotActions.add(a.asText()));
            }

            return Optional.of(AIRecommendationResponse.builder()
                    .recommendationType(recType)
                    .priority(priority)
                    .title(title)
                    .summary(summary)
                    .explanation(explanation)
                    .actions(actions)
                    .doNotActions(doNotActions)
                    .limitations(limitations)
                    .confidenceScore(BigDecimal.valueOf(conf))
                    .providerName(getProviderName())
                    .build());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Gemini API call was interrupted: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Exception during Gemini AI recommendation generation: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getProviderName() {
        return "GEMINI_" + (aiProperties != null && aiProperties.getGemini() != null ? aiProperties.getGemini().getModel() : "DEFAULT");
    }

    @Override
    public boolean isAvailable() {
        return aiProperties != null
                && aiProperties.getGemini() != null
                && aiProperties.getGemini().getApiKey() != null
                && !aiProperties.getGemini().getApiKey().isBlank();
    }

    private record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {}
    private record Content(String role, List<Part> parts) {}
    private record Part(String text) {}
    private record GenerationConfig(double temperature, String responseMimeType) {}
}
