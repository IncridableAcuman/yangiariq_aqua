package com.aquaai.recommendation.domain.port;

import com.aquaai.recommendation.domain.model.AIRecommendationResponse;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;

import java.util.Optional;

/**
 * Domain port for advisory reasoning providers (e.g. Gemini, Anthropic, or Local LLM).
 */
public interface AgronomicAIProvider {

    Optional<AIRecommendationResponse> generateRecommendation(AgronomicRecommendationContext context);

    String getProviderName();

    boolean isAvailable();
}
