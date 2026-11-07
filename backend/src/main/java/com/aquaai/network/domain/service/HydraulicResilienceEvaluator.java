package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.HydraulicSurgeRisk;
import com.aquaai.network.domain.model.ResilienceScenario;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Bounded hydraulic resilience & operational scenario analysis engine.
 * Computes disruption impacts and operational surge precautions without claiming
 * unvalidated transient engineering simulation.
 */
public class HydraulicResilienceEvaluator {

    private static final BigDecimal MAX_SAFE_RAMPING_RATE_PER_HR = new BigDecimal("0.50"); // 0.5 m³/s per hour

    /**
     * Evaluates operational hydraulic surge risk when dispatching a planned flow rate change.
     */
    public HydraulicSurgeRisk evaluateSurgeRisk(
            WaterNetworkEdge edge,
            BigDecimal requestedFlowChangeM3S,
            BigDecimal proposedDurationHours
    ) {
        if (edge == null) {
            return HydraulicSurgeRisk.builder()
                    .riskStatus("UNKNOWN")
                    .surgeIndexScore(BigDecimal.ZERO)
                    .abruptChangeDetected(false)
                    .maxPermissibleRampingRateM3SPerHour(MAX_SAFE_RAMPING_RATE_PER_HR)
                    .recommendedAction("Verify canal edge configuration.")
                    .explanation("Segment data unavailable.")
                    .build();
        }

        BigDecimal deltaFlow = requestedFlowChangeM3S != null ? requestedFlowChangeM3S.abs() : BigDecimal.ZERO;
        BigDecimal hours = (proposedDurationHours != null && proposedDurationHours.compareTo(BigDecimal.ZERO) > 0)
                ? proposedDurationHours : BigDecimal.ONE;

        BigDecimal rampingRate = deltaFlow.divide(hours, 2, RoundingMode.HALF_UP);
        boolean abrupt = rampingRate.compareTo(MAX_SAFE_RAMPING_RATE_PER_HR) > 0;

        BigDecimal surgeIndex = rampingRate.multiply(new BigDecimal("100.00"))
                .divide(edge.getMaxCapacityM3S(), 2, RoundingMode.HALF_UP);
        if (surgeIndex.compareTo(new BigDecimal("100.00")) > 0) {
            surgeIndex = new BigDecimal("100.00");
        }

        String status = abrupt ? "CONSTRAINED" : "SAFE";
        String action = abrupt
                ? "Implement staged gate opening: Ramp flow gradually over at least " + deltaFlow.divide(MAX_SAFE_RAMPING_RATE_PER_HR, 1, RoundingMode.UP) + " hours to prevent canal bank scouring and hydraulic shock."
                : "Proceed with standard valve/gate dispatch schedule.";
        String explanation = abrupt
                ? "Calculated ramping rate (" + rampingRate + " m³/s/hr) exceeds safe threshold (" + MAX_SAFE_RAMPING_RATE_PER_HR + " m³/s/hr)."
                : "Ramping rate is within safe operational limits.";

        return HydraulicSurgeRisk.builder()
                .segmentId(edge.getId())
                .segmentName(edge.getName())
                .riskStatus(status)
                .surgeIndexScore(surgeIndex)
                .abruptChangeDetected(abrupt)
                .maxPermissibleRampingRateM3SPerHour(MAX_SAFE_RAMPING_RATE_PER_HR)
                .recommendedAction(action)
                .explanation(explanation)
                .build();
    }

    /**
     * Simulates an isolated resilience scenario (canal outage, pump failure, or drought deficit).
     */
    public ResilienceScenario simulateScenario(
            String scenarioName,
            String scenarioType,
            List<UUID> disabledNodeIds,
            List<UUID> disabledEdgeIds,
            BigDecimal capacityReductionPercent,
            BigDecimal demandSurgePercent,
            List<WaterNetworkNode> allNodes,
            List<WaterNetworkEdge> allEdges,
            Map<UUID, BigDecimal> fieldDemandMap,
            UUID createdBy
    ) {
        Set<UUID> disabledNodes = disabledNodeIds != null ? new HashSet<>(disabledNodeIds) : Collections.emptySet();
        Set<UUID> disabledEdges = disabledEdgeIds != null ? new HashSet<>(disabledEdgeIds) : Collections.emptySet();

        BigDecimal lostCapacity = BigDecimal.ZERO;
        for (WaterNetworkEdge edge : allEdges) {
            if (disabledEdges.contains(edge.getId()) || disabledNodes.contains(edge.getStartNodeId()) || disabledNodes.contains(edge.getEndNodeId())) {
                lostCapacity = lostCapacity.add(edge.getMaxCapacityM3S());
            }
        }

        // Identify disconnected field inlet nodes
        PathResolutionEngine pathEngine = new PathResolutionEngine();
        Set<UUID> affectedFieldIds = new HashSet<>();
        List<String> affectedWuas = new ArrayList<>();

        for (UUID edgeId : disabledEdges) {
            List<UUID> downstreamFields = pathEngine.findDownstreamFieldIds(edgeId, allNodes, allEdges);
            affectedFieldIds.addAll(downstreamFields);
        }

        for (WaterNetworkNode node : allNodes) {
            if (disabledNodes.contains(node.getId()) && node.getType() == WaterNetworkNodeType.FIELD_INLET && node.getFieldId() != null) {
                affectedFieldIds.add(node.getFieldId());
            }
        }

        BigDecimal unmetDemand = BigDecimal.ZERO;
        if (fieldDemandMap != null) {
            for (UUID fieldId : affectedFieldIds) {
                BigDecimal demand = fieldDemandMap.getOrDefault(fieldId, BigDecimal.ZERO);
                unmetDemand = unmetDemand.add(demand);
            }
        }

        if (demandSurgePercent != null && demandSurgePercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal surgeFactor = BigDecimal.ONE.add(demandSurgePercent.divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP));
            unmetDemand = unmetDemand.multiply(surgeFactor).setScale(2, RoundingMode.HALF_UP);
        }

        String summary = String.format(
                "Scenario '%s' (%s): %d parcels affected, lost conveyance capacity: %s m³/s, estimated unmet water volume: %s m³.",
                scenarioName, scenarioType, affectedFieldIds.size(), lostCapacity, unmetDemand
        );

        Map<String, Object> results = new HashMap<>();
        results.put("affectedFieldIds", new ArrayList<>(affectedFieldIds));
        results.put("simulationTimestamp", Instant.now().toString());
        results.put("assumptions", "Linear disruption propagation; zero live actuator interaction.");

        return ResilienceScenario.builder()
                .name(scenarioName)
                .scenarioType(scenarioType)
                .disabledNodeIds(disabledNodeIds)
                .disabledEdgeIds(disabledEdgeIds)
                .capacityReductionPercent(capacityReductionPercent)
                .demandSurgePercent(demandSurgePercent)
                .affectedFieldCount(affectedFieldIds.size())
                .lostCapacityM3S(lostCapacity)
                .unmetDemandM3(unmetDemand)
                .affectedWuaNames(affectedWuas)
                .alternativeFeasibleRoutesCount(0) // Default conservative estimate
                .summary(summary)
                .simulationResults(results)
                .createdBy(createdBy)
                .build();
    }
}
