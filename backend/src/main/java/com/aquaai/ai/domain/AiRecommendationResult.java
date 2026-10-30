package com.aquaai.ai.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record AiRecommendationResult(
        String summary,
        BigDecimal recommendedWaterVolumeCubicMeters,
        String recommendedIrrigationWindow,
        String riskLevel,
        List<String> keyActionSteps,
        String reasoning,
        String providerName,
        Instant generatedAt
) {
}
