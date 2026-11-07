package com.aquaai.network.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ResilienceScenarioRequest(
        @NotBlank(message = "Scenario name is required")
        String name,
        @NotBlank(message = "Scenario type is required")
        String scenarioType, // CANAL_OUTAGE, PUMP_FAILURE, SOURCE_DROUGHT_DEFICIT, CAPACITY_RESTRICTION, DEMAND_SURGE
        List<UUID> disabledNodeIds,
        List<UUID> disabledEdgeIds,
        BigDecimal capacityReductionPercent,
        BigDecimal demandSurgePercent
) {}
