package com.aquaai.recommendation.domain.port;

import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;

import java.util.List;

/**
 * Domain port for the rule-based deterministic recommendation engine.
 */
public interface AgronomicRecommendationEngine {

    List<AgronomicRecommendation> evaluate(AgronomicRecommendationContext context);
}
