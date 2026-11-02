package com.aquaai.analytics.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class IrrigationPerformanceDto {
    private final long totalPlans;
    private final long proposedCount;
    private final long approvedCount;
    private final long scheduledCount;
    private final long inProgressCount;
    private final long completedCount;
    private final long cancelledCount;
    private final long rejectedCount;
    private final long blockedCount;
    private final BigDecimal operationCompletionRate;
    private final BigDecimal planAdherencePercent;
    private final int averagePlannedDurationMinutes;
    private final int averageActualDurationMinutes;
    private final String adherenceExplanation;

    @JsonProperty("totalPlansCreated")
    public long getTotalPlansCreated() {
        return totalPlans;
    }

    @JsonProperty("totalPlansApproved")
    public long getTotalPlansApproved() {
        return approvedCount;
    }

    @JsonProperty("totalOperationsScheduled")
    public long getTotalOperationsScheduled() {
        return scheduledCount;
    }

    @JsonProperty("totalOperationsCompleted")
    public long getTotalOperationsCompleted() {
        return completedCount;
    }

    @JsonProperty("executionRatePercent")
    public BigDecimal getExecutionRatePercent() {
        return operationCompletionRate;
    }

    @JsonProperty("averageDelayMinutes")
    public int getAverageDelayMinutes() {
        return Math.max(0, averageActualDurationMinutes - averagePlannedDurationMinutes);
    }
}
