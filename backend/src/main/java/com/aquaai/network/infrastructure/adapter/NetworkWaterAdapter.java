package com.aquaai.network.infrastructure.adapter;

import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.network.domain.port.NetworkWaterPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NetworkWaterAdapter implements NetworkWaterPort {

    private final WaterBudgetDataPort waterBudgetDataPort;

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
    public BigDecimal getRemainingAllocationVolume(UUID fieldId) {
        if (fieldId == null) return BigDecimal.ZERO;
        return waterBudgetDataPort.getWaterBudgetForField(fieldId)
                .map(WaterBudget::getRemainingAvailableM3)
                .orElse(BigDecimal.ZERO);
    }
}
