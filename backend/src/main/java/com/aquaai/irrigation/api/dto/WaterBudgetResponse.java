package com.aquaai.irrigation.api.dto;

import com.aquaai.irrigation.domain.model.WaterBudget;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class WaterBudgetResponse {

    private final UUID fieldId;
    private final BigDecimal totalAllocatedM3;
    private final BigDecimal consumedM3;
    private final BigDecimal committedM3;
    private final BigDecimal reservedM3;
    private final BigDecimal remainingAvailableM3;

    public static WaterBudgetResponse fromDomain(WaterBudget b) {
        if (b == null) {
            return null;
        }
        return WaterBudgetResponse.builder()
                .fieldId(b.getFieldId())
                .totalAllocatedM3(b.getTotalAllocatedM3())
                .consumedM3(b.getConsumedM3())
                .committedM3(b.getCommittedM3())
                .reservedM3(b.getReservedM3())
                .remainingAvailableM3(b.getRemainingAvailableM3())
                .build();
    }
}
