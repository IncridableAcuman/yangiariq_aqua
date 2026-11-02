package com.aquaai.analytics.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class PerformanceTrendPointDto {
    private final Instant timestamp;
    private final String dateLabel;
    private final BigDecimal waterConsumedM3;
    private final BigDecimal waterPlannedM3;
    private final BigDecimal utilizationPercent;
    private final int completedOperationsCount;
    private final BigDecimal telemetryCoveragePercent;
    private final int safetyEventsCount;
}
