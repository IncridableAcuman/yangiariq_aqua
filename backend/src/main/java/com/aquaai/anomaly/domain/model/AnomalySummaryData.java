package com.aquaai.anomaly.domain.model;

/**
 * High-level summary metrics across active alerts and system health.
 */
public record AnomalySummaryData(
        long totalActive,
        long criticalCount,
        long highCount,
        long mediumCount,
        long lowCount,
        long infoCount,
        long detectedCount,
        long acknowledgedCount,
        long investigatingCount,
        long resolvedCount,
        long dismissedCount
) {
}
