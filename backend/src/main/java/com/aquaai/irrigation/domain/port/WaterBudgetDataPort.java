package com.aquaai.irrigation.domain.port;

import com.aquaai.irrigation.domain.model.WaterBudget;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for querying volumetric water allocation quotas and consumption budgets for parcels.
 */
public interface WaterBudgetDataPort {

    Optional<WaterBudget> getWaterBudgetForField(UUID fieldId);

    Optional<UUID> getActiveWaterAllocationId(UUID fieldId);
}
