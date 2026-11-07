package com.aquaai.network.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ResilienceScenarioResponse(
        UUID id,
        String name,
        String scenarioType,
        List<UUID> disabledNodeIds,
        List<UUID> disabledEdgeIds,
        BigDecimal capacityReductionPercent,
        BigDecimal demandSurgePercent,
        int affectedFieldCount,
        BigDecimal lostCapacityM3S,
        BigDecimal unmetDemandM3,
        List<String> affectedWuaNames,
        int alternativeFeasibleRoutesCount,
        String summary,
        Map<String, Object> simulationResults,
        UUID createdBy,
        Instant createdAt
) {}
