package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WaterBudgetSummaryDto {
    private final BigDecimal totalAllocatedM3;
    private final BigDecimal consumedM3;
    private final BigDecimal committedM3;
    private final BigDecimal reservedM3;
    private final BigDecimal remainingAvailableM3;
    private final boolean isExhausted;
    private final BigDecimal utilizationPercentage;
}
