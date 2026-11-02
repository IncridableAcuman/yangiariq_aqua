package com.aquaai.analytics.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FarmOverviewMetricsDto {
    private final int totalFields;
    private final int activeFields;
    private final BigDecimal totalAreaHectares;
    private final BigDecimal totalAllocatedM3;
    private final BigDecimal totalConsumedM3;
    private final BigDecimal totalRemainingM3;
    private final long totalCompletedOperations;
    private final long totalCancelledOperations;
    private final long totalBlockedOperations;
    private final int totalSensors;
    private final int healthySensors;
    private final int staleSensors;
    private final int offlineSensors;
    private final long criticalSafetyEvents;
    private final long warningSafetyEvents;

    @JsonProperty("aggregatePerformanceScore")
    public int getAggregatePerformanceScore() {
        return criticalSafetyEvents > 0 ? 55 : (warningSafetyEvents > 0 ? 75 : 90);
    }

    @JsonProperty("overallStatus")
    public String getOverallStatus() {
        return criticalSafetyEvents > 0 ? "ATTENTION" : (warningSafetyEvents > 0 ? "WARNING" : "NORMAL");
    }

    @JsonProperty("totalSafetyEvents")
    public long getTotalSafetyEvents() {
        return criticalSafetyEvents + warningSafetyEvents;
    }

    @JsonProperty("criticalAnomaliesCount")
    public long getCriticalAnomaliesCount() {
        return criticalSafetyEvents;
    }
}
