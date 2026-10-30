package com.aquaai.ai.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record AiRecommendationRequest(
        UUID fieldId,
        String cropType,
        String soilType,
        BigDecimal fieldAreaHectares,
        BigDecimal currentSoilMoisturePercent,
        BigDecimal recentWaterAllocationCubicMeters,
        String weatherForecastSummary,
        Map<String, Object> additionalContext
) {
}
