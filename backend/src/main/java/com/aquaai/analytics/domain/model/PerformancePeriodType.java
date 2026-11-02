package com.aquaai.analytics.domain.model;

/**
 * Supported temporal filter windows for farm performance and historical intelligence analytics.
 */
public enum PerformancePeriodType {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_90_DAYS,
    SEASON,
    CUSTOM
}
