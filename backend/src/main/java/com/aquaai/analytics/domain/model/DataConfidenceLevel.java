package com.aquaai.analytics.domain.model;

/**
 * Data quality and reporting completeness confidence classifications.
 * Distinguishes trustworthy analytics from partial or insufficient sensor telemetry.
 */
public enum DataConfidenceLevel {
    /**
     * High telemetry and verification coverage (>= 85%).
     * Analytical conclusions are robust and decision-grade.
     */
    COMPLETE,

    /**
     * Moderate telemetry or verification coverage (50% - 84%).
     * Some data points are interpolated or missing.
     */
    PARTIAL,

    /**
     * Low data availability (< 50%) or absent telemetry.
     * Conclusions are tentative; physical field inspection is recommended.
     */
    INSUFFICIENT
}
