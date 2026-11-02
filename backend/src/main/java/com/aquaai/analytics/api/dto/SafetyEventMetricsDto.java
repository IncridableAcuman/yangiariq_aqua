package com.aquaai.analytics.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SafetyEventMetricsDto {
    private final long totalEvents;
    private final long criticalCount;
    private final long highCount;
    private final long mediumCount;
    private final long lowCount;
    private final int repeatedSafetyFieldCount;
    private final int activeBlockedParcelsCount;

    @JsonProperty("totalAnomaliesDetected")
    public long getTotalAnomaliesDetected() {
        return totalEvents;
    }

    @JsonProperty("criticalAnomaliesCount")
    public long getCriticalAnomaliesCount() {
        return criticalCount;
    }

    @JsonProperty("warningAnomaliesCount")
    public long getWarningAnomaliesCount() {
        return highCount;
    }

    @JsonProperty("activeHydraulicBlocksCount")
    public int getActiveHydraulicBlocksCount() {
        return activeBlockedParcelsCount;
    }

    @JsonProperty("safetyIncidentRatePer100Ha")
    public double getSafetyIncidentRatePer100Ha() {
        return totalEvents > 0 ? (double) totalEvents : 0.0;
    }
}
