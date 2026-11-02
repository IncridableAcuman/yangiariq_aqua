package com.aquaai.analytics.domain.model;

/**
 * Performance evaluation classification for agricultural parcels.
 * Governed strictly by deterministic thresholds; safety interlocks always dominate.
 */
public enum FieldPerformanceStatus {
    /**
     * Water utilization <= 80%, plan adherence >= 80%, telemetry healthy, zero critical safety events.
     */
    NORMAL,

    /**
     * Water utilization 80-100%, plan adherence 60-79%, or degraded sensor telemetry.
     */
    WARNING,

    /**
     * Water over-consumed (> 100%), severe plan non-adherence (< 60%),
     * or active critical hydraulic safety events.
     */
    ATTENTION
}
