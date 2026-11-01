package com.aquaai.irrigation.infrastructure.adapter;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WaterBudgetDataAdapter implements WaterBudgetDataPort {

    private final WaterAllocationRepository waterAllocationRepository;
    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;
    private final SmartIrrigationPlanRepository irrigationPlanRepository;

    private static final Set<IrrigationPlanStatus> COMMITTED_STATUSES = Set.of(
            IrrigationPlanStatus.SCHEDULED,
            IrrigationPlanStatus.IN_PROGRESS
    );

    private static final Set<IrrigationPlanStatus> RESERVED_STATUSES = Set.of(
            IrrigationPlanStatus.APPROVED,
            IrrigationPlanStatus.PROPOSED
    );

    @Override
    public Optional<WaterBudget> getWaterBudgetForField(UUID fieldId) {
        if (fieldId == null) {
            return Optional.empty();
        }

        List<WaterAllocation> activeAllocations = waterAllocationRepository.findByFieldIdAndStatus(
                fieldId,
                WaterAllocationStatus.ACTIVE
        );

        BigDecimal totalAllocated = BigDecimal.ZERO;
        BigDecimal totalConsumed = BigDecimal.ZERO;

        for (WaterAllocation alloc : activeAllocations) {
            totalAllocated = totalAllocated.add(alloc.getAllocatedVolumeM3());
            BigDecimal consumed = waterConsumptionQueryRepository.getConsumedVolumeByAllocationId(alloc.getId());
            if (consumed != null) {
                totalConsumed = totalConsumed.add(consumed);
            }
        }

        BigDecimal committed = irrigationPlanRepository.sumVolumeByFieldIdAndStatuses(fieldId, COMMITTED_STATUSES);
        BigDecimal reserved = irrigationPlanRepository.sumVolumeByFieldIdAndStatuses(fieldId, RESERVED_STATUSES);

        WaterBudget budget = WaterBudget.calculate(
                fieldId,
                totalAllocated,
                totalConsumed,
                committed,
                reserved
        );

        return Optional.of(budget);
    }

    @Override
    public Optional<UUID> getActiveWaterAllocationId(UUID fieldId) {
        if (fieldId == null) {
            return Optional.empty();
        }
        List<WaterAllocation> activeAllocations = waterAllocationRepository.findByFieldIdAndStatus(
                fieldId,
                WaterAllocationStatus.ACTIVE
        );
        if (activeAllocations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(activeAllocations.get(0).getId());
    }
}
