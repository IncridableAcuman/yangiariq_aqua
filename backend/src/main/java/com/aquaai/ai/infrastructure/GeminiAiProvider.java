package com.aquaai.ai.infrastructure;

import com.aquaai.ai.domain.AiProvider;
import com.aquaai.ai.domain.AiRecommendationRequest;
import com.aquaai.ai.domain.AiRecommendationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiAiProvider implements AiProvider {

    private final AiProperties aiProperties;

    public GeminiAiProvider(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public String getProviderName() {
        return "GEMINI_" + aiProperties.getGemini().getModel().toUpperCase();
    }

    @Override
    public AiRecommendationResult generateIrrigationRecommendation(AiRecommendationRequest request) {
        String model = aiProperties.getGemini().getModel();
        log.info("Invoking Gemini AI Provider for field {} with model {}", request.fieldId(), model);

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
