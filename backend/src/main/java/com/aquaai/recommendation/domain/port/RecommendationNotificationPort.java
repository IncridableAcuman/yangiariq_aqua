package com.aquaai.recommendation.domain.port;

import com.aquaai.recommendation.domain.model.AgronomicRecommendation;

/**
 * Domain port for notifying farmers or operators of high-priority agronomic proposals.
 */
public interface RecommendationNotificationPort {

    void sendRecommendationAlert(AgronomicRecommendation recommendation);
}
