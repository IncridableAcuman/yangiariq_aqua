package com.aquaai.prediction.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AdaptiveOverviewResponse(
        int totalAdaptiveMethodsCount,
        int activeVersionsCount,
        long contextAwareSelectionsCount,
        long baselineFallbackCount,
        long insufficientContextCount,
        boolean contextualDriftWarningActive,
        List<AdaptiveSelectionDecisionDto> recentSelections,
        List<ContextPerformanceProfileDto> contextualProfiles
) {}
