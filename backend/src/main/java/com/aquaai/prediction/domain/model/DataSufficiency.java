package com.aquaai.prediction.domain.model;

/**
 * Audit status indicating whether the underlying telemetry and operational history
 * are adequate to calculate a reliable forecast.
 */
public enum DataSufficiency {
    /**
     * Data is fresh, frequent, and complete. High confidence forecast possible.
     */
    SUFFICIENT,

    /**
     * Minimum observation threshold met, but observations may have gaps or moderate staleness.
     */
    LIMITED,

    /**
     * Missing, severely stale (>24h), or insufficient observations (< minimum threshold).
     * Predictive engine must report UNKNOWN / UNAVAILABLE and never fabricate data.
     */
    INSUFFICIENT
}
