package com.aquaai.recommendation.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validated structured response contract returned by the advisory LLM provider.
 */
@Builder
public record AIRecommendationResponse(
        RecommendationType recommendationType,
        RecommendationPriority priority,
        String title,
        String summary,
        String explanation,
        List<String> actions,
        List<String> doNotActions,
        String limitations,
        BigDecimal confidenceScore,
        String providerName
) {
}
