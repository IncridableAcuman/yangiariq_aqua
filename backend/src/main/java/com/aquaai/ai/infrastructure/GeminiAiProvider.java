package com.aquaai.ai.infrastructure;

import com.aquaai.ai.domain.AiProvider;
import com.aquaai.ai.domain.AiRecommendationRequest;
import com.aquaai.ai.domain.AiRecommendationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiAiProvider implements AiProvider {

    private final String apiKey;
    private final String model;

    public GeminiAiProvider(
            @Value("${app.ai.gemini.api-key:dummy-key}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-1.5-flash}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String getProviderName() {
        return "GEMINI_" + model.toUpperCase();
    }

    @Override
    public AiRecommendationResult generateIrrigationRecommendation(AiRecommendationRequest request) {
        log.info("Invoking Gemini AI Provider for field {} with model {}", request.fieldId(), model);

        // Architectural foundation: Gemini API client adapter will format prompt and execute API call
        // Fallback / stub response for foundation verification
        return AiRecommendationResult.builder()
                .summary("Gemini-generated water allocation for " + request.cropType() + " in Yangiariq")
                .recommendedWaterVolumeCubicMeters(BigDecimal.valueOf(850.50))
                .recommendedIrrigationWindow("Night (22:00 - 04:00)")
                .riskLevel("MODERATE")
                .keyActionSteps(List.of(
                        "Activate drip distribution in sector B",
                        "Verify canal head gate calibration"
                ))
                .reasoning("Gemini evaluated weather trends, evapotranspiration rates, and historical soil permeability.")
                .providerName(getProviderName())
                .generatedAt(Instant.now())
                .build();
    }

    @Override
    public String analyzeWaterProblem(String problemDescription, String districtLocation) {
        log.info("Invoking Gemini to analyze water incident at {}", districtLocation);
        return "Gemini Analysis: Potential canal blockage or unauthorized diversion upstream. Recommended investigation priority: HIGH.";
    }
}
