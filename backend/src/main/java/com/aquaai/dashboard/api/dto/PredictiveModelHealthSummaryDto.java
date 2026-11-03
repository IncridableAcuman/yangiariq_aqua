package com.aquaai.dashboard.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictiveModelHealthSummaryDto {
    private int activeVersions;
    private int healthy;
    private int watch;
    private int candidates;
    private int degraded;
    private boolean driftDetected;
    private long contextAwareSelections;
    private boolean contextualDriftDetected;
}
