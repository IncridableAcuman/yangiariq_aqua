package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WaterMetricsDto {
    private final BigDecimal totalAllocatedM3;
    private final BigDecimal totalConsumedM3;
    private final BigDecimal totalRemainingM3;
    private final BigDecimal totalCommittedM3;
    private final BigDecimal totalReservedM3;
    private final BigDecimal totalOverConsumedM3;
    private final int overConsumedFieldCount;
    private final BigDecimal overallConsumptionPercentage;
}
