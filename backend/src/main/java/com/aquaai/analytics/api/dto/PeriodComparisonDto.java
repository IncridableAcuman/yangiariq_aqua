package com.aquaai.analytics.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PeriodComparisonDto {
    private final boolean hasComparisonData;
    private final String previousPeriodLabel;
    private final BigDecimal waterConsumedDeltaPercent;
    private final BigDecimal waterConsumedDeltaM3;
    private final long operationsCompletedDelta;
    private final BigDecimal planAdherenceDeltaPercent;
    private final BigDecimal telemetryCoverageDeltaPercent;
    private final long safetyEventsDelta;
    private final String comparisonSummary;
}
