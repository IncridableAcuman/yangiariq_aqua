package com.aquaai.network.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Isolated simulation scenario evaluating network disruption impacts
 * (canal rupture, pump station failure, drought deficit).
 * Never alters live operational state.
 */
@Getter
@Builder(toBuilder = true)
public class ResilienceScenario {

    private final UUID id;
    private final String name;
    private final String scenarioType; // CANAL_OUTAGE, PUMP_FAILURE, SOURCE_DROUGHT_DEFICIT, CAPACITY_RESTRICTION, DEMAND_SURGE
    private final List<UUID> disabledNodeIds;
    private final List<UUID> disabledEdgeIds;
    private final BigDecimal capacityReductionPercent;
    private final BigDecimal demandSurgePercent;
    private final int affectedFieldCount;
    private final BigDecimal lostCapacityM3S;
    private final BigDecimal unmetDemandM3;
    private final List<String> affectedWuaNames;
    private final int alternativeFeasibleRoutesCount;
    private final String summary;
    private final Map<String, Object> simulationResults;
    private final UUID createdBy;
    private final Instant createdAt;

    public ResilienceScenario(
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
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "Scenario name must not be null");
        this.scenarioType = Objects.requireNonNull(scenarioType, "scenarioType must not be null");
        this.disabledNodeIds = disabledNodeIds != null ? Collections.unmodifiableList(new ArrayList<>(disabledNodeIds)) : Collections.emptyList();
        this.disabledEdgeIds = disabledEdgeIds != null ? Collections.unmodifiableList(new ArrayList<>(disabledEdgeIds)) : Collections.emptyList();
        this.capacityReductionPercent = capacityReductionPercent != null ? capacityReductionPercent : BigDecimal.ZERO;
        this.demandSurgePercent = demandSurgePercent != null ? demandSurgePercent : BigDecimal.ZERO;
        this.affectedFieldCount = affectedFieldCount;
        this.lostCapacityM3S = lostCapacityM3S != null ? lostCapacityM3S : BigDecimal.ZERO;
        this.unmetDemandM3 = unmetDemandM3 != null ? unmetDemandM3 : BigDecimal.ZERO;
        this.affectedWuaNames = affectedWuaNames != null ? Collections.unmodifiableList(new ArrayList<>(affectedWuaNames)) : Collections.emptyList();
        this.alternativeFeasibleRoutesCount = alternativeFeasibleRoutesCount;
        this.summary = summary != null ? summary : "Resilience simulation execution completed.";
        this.simulationResults = simulationResults != null ? Collections.unmodifiableMap(simulationResults) : Collections.emptyMap();
        this.createdBy = createdBy;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}
