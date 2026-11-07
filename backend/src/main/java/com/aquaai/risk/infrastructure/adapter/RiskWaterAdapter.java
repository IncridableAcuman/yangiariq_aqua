package com.aquaai.risk.infrastructure.adapter;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.risk.domain.port.RiskWaterPort;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskWaterAdapter implements RiskWaterPort {

    private final WaterBudgetDataPort waterBudgetDataPort;
    private final WaterAllocationRepository waterAllocationRepository;
    private final SmartIrrigationPlanRepository smartPlanRepository;

    @Override
    public Optional<WaterBudget> getWaterBudget(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        return waterBudgetDataPort.getWaterBudgetForField(fieldId);
    }

    @Override
    public boolean hasActiveAllocation(UUID fieldId) {
        if (fieldId == null) return false;
        return waterBudgetDataPort.getActiveWaterAllocationId(fieldId).isPresent();
    }

    @Override
    public BigDecimal getTotalUpcomingPlannedVolumeM3(UUID fieldId) {
        if (fieldId == null) return BigDecimal.ZERO;
        List<SmartIrrigationPlan> plans = smartPlanRepository.findByFieldId(fieldId);
        if (plans == null || plans.isEmpty()) return BigDecimal.ZERO;

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        BigDecimal sum = BigDecimal.ZERO;
        for (SmartIrrigationPlan p : plans) {
            if ((p.getStatus() == IrrigationPlanStatus.SCHEDULED || p.getStatus() == IrrigationPlanStatus.APPROVED)
                    && p.getPlannedDate() != null
                    && !p.getPlannedDate().isBefore(today)
                    && !p.getPlannedDate().isAfter(nextWeek)) {
                if (p.getRecommendedVolumeM3() != null) {
                    sum = sum.add(p.getRecommendedVolumeM3());
                }
            }
        }
        return sum;
    }
}
