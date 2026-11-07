package com.aquaai.network.domain.port;

import com.aquaai.irrigation.domain.model.WaterBudget;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface NetworkWaterPort {
    Optional<WaterBudget> getWaterBudget(UUID fieldId);
    boolean hasActiveAllocation(UUID fieldId);
    BigDecimal getRemainingAllocationVolume(UUID fieldId);
}
