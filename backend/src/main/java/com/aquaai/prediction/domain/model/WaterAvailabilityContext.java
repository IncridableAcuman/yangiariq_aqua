package com.aquaai.prediction.domain.model;

/**
 * Normalized seasonal water availability and canal supply status.
 */
public enum WaterAvailabilityContext {
    ABUNDANT,    // Remaining quota > 50% with high canal flow
    NORMAL,      // Remaining quota 20-50%
    CONSTRAINED, // Remaining quota 5-19% or canal rationing active
    CRITICAL,    // Quota exhausted (< 5%) or over-consumed
    UNKNOWN
}
