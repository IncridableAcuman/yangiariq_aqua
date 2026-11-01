package com.aquaai.anomaly.api.dto;

import com.aquaai.anomaly.domain.model.AnomalySummaryData;

public record AnomalySummaryResponse(
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
    public static AnomalySummaryResponse fromDomain(AnomalySummaryData domain) {
        if (domain == null) return null;
        return new AnomalySummaryResponse(
                domain.totalActive(),
                domain.criticalCount(),
                domain.highCount(),
                domain.mediumCount(),
                domain.lowCount(),
                domain.infoCount(),
                domain.detectedCount(),
                domain.acknowledgedCount(),
                domain.investigatingCount(),
                domain.resolvedCount(),
                domain.dismissedCount()
        );
    }
}
