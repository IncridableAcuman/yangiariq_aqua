package com.aquaai.intelligence.domain.model;

/**
 * Evaluated freshness status of incoming sensor telemetry.
 */
public enum TelemetryFreshness {
    FRESH,
    STALE,
    MISSING
}
