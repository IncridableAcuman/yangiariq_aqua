package com.aquaai.recommendation.domain.model;

/**
 * Finite lifecycle states for an agronomic recommendation.
 */
public enum RecommendationStatus {
    GENERATED,
    VIEWED,
    ACCEPTED,
    DISMISSED,
    EXPIRED,
    COMPLETED
}
