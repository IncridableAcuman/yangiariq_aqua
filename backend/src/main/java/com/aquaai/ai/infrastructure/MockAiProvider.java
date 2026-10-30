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
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProvider implements AiProvider {

    @Override
    public String getProviderName() {
        return "MOCK_AI_PROVIDER";
    }

    @Override
    public AiRecommendationResult generateIrrigationRecommendation(AiRecommendationRequest request) {
        log.info("Generating mock irrigation recommendation for field: {}", request.fieldId());

        BigDecimal defaultAllocation = request.fieldAreaHectares() != null
                ? request.fieldAreaHectares().multiply(BigDecimal.valueOf(450))
                : BigDecimal.valueOf(1200);

        return AiRecommendationResult.builder()
                .summary("Simulated water conservation recommendation for Yangiariq field.")
                .recommendedWaterVolumeCubicMeters(defaultAllocation)
                .recommendedIrrigationWindow("Evening (19:00 - 23:00) to minimize evaporative loss")
                .riskLevel("LOW")
                .keyActionSteps(List.of(
                        "Inspect secondary delivery furrow for sedimentation",
                        "Maintain root-zone moisture between 60% and 75%",
                        "Record meter volume upon gate closure"
                ))
                .reasoning("Calculated based on Yangiariq soil profile and current simulated crop evapotranspiration.")
                .providerName(getProviderName())
                .generatedAt(Instant.now())
                .build();
    }

    @Override
    public String analyzeWaterProblem(String problemDescription, String districtLocation) {
        log.info("Analyzing water problem via mock provider: location={}", districtLocation);
        return "Simulated analysis: Issue appears related to canal distribution scheduling. Priority: NORMAL.";
    }
}
