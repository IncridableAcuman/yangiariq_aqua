package com.aquaai.analytics.api.dto;

import com.aquaai.analytics.domain.model.PerformancePeriodType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PerformancePeriodDto {
    private final PerformancePeriodType type;
    private final Instant from;
    private final Instant to;
    private final String label;
    private final long durationDays;
}
