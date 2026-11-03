package com.aquaai.prediction.domain.model;

/**
 * Health status of predictive algorithms, models, and decision rules.
 */
public enum ModelHealthStatus {
    HEALTHY,
    WATCH,
    DEGRADED,
    INSUFFICIENT_DATA,
    CANDIDATE_AVAILABLE
}
