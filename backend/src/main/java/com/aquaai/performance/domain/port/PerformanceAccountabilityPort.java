package com.aquaai.performance.domain.port;

import java.math.BigDecimal;

public interface PerformanceAccountabilityPort {

    record IncidentPerformanceMetrics(
            long totalIncidents,
            long underReviewCount,
            long investigatingCount,
            long verifiedResolvedCount,
            long closedCount,
            long overdueCount,
            long recurringCount,
            BigDecimal verifiedResolutionRatePct,
            BigDecimal recurrenceRatePct,
            BigDecimal averageReviewHours,
            BigDecimal averageResolutionHours
    ) {}

    IncidentPerformanceMetrics getIncidentPerformanceMetrics();
}
