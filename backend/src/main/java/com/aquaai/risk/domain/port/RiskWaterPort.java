package com.aquaai.risk.domain.port;

import com.aquaai.irrigation.domain.model.WaterBudget;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface RiskWaterPort {

    Optional<WaterBudget> getWaterBudget(UUID fieldId);

    boolean hasActiveAllocation(UUID fieldId);

    BigDecimal getTotalUpcomingPlannedVolumeM3(UUID fieldId);
}
