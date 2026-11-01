package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IrrigationMetricsDto {
    private final int draftCount;
    private final int proposedCount;
    private final int approvedCount;
    private final int scheduledTodayCount;
    private final int inProgressCount;
    private final int completedCount;
    private final int blockedCount;
}
