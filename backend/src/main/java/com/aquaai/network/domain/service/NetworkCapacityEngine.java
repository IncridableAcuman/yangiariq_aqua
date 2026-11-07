package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.HydraulicConstraint;
import com.aquaai.network.domain.model.NetworkCapacity;
import com.aquaai.network.domain.model.OperationalStatus;
import com.aquaai.network.domain.model.WaterNetworkEdge;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deterministic hydraulic capacity & constraint evaluation engine.
 * Computes segment utilization, identifies bottlenecks, and strictly enforces
 * hydraulic safety interlocks over conveyance proposals.
 */
public class NetworkCapacityEngine {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal NINETY = new BigDecimal("90.00");

    public NetworkCapacity evaluateSegmentCapacity(
            WaterNetworkEdge edge,
            BigDecimal plannedFlowM3S,
            boolean isHydraulicInterlockActive,
            String interlockReason
    ) {
        BigDecimal maxCap = edge.getMaxCapacityM3S();
        BigDecimal currentFlow = edge.getCurrentFlowM3S() != null ? edge.getCurrentFlowM3S() : BigDecimal.ZERO;
        BigDecimal plannedFlow = plannedFlowM3S != null ? plannedFlowM3S : BigDecimal.ZERO;

        BigDecimal totalCommittedFlow = currentFlow.add(plannedFlow);
        BigDecimal remainingCap = maxCap.subtract(totalCommittedFlow);
        if (remainingCap.compareTo(BigDecimal.ZERO) < 0) {
            remainingCap = BigDecimal.ZERO;
        }

        BigDecimal utilizationPercent = BigDecimal.ZERO;
        if (maxCap.compareTo(BigDecimal.ZERO) > 0) {
            utilizationPercent = totalCommittedFlow.multiply(HUNDRED).divide(maxCap, 2, RoundingMode.HALF_UP);
        }

        boolean isOverloaded = totalCommittedFlow.compareTo(maxCap) > 0;

        String constraintStatus = "SAFE";
        if (isHydraulicInterlockActive || edge.getOperationalStatus() == OperationalStatus.BLOCKED) {
            constraintStatus = "BLOCKED";
        } else if (edge.getOperationalStatus() == OperationalStatus.MAINTENANCE || edge.getOperationalStatus() == OperationalStatus.OFFLINE) {
            constraintStatus = "CONSTRAINED";
        } else if (isOverloaded || utilizationPercent.compareTo(NINETY) >= 0) {
            constraintStatus = "CONSTRAINED";
        }

        return NetworkCapacity.builder()
                .segmentId(edge.getId())
                .segmentName(edge.getName())
                .edgeType(edge.getType())
                .maxCapacityM3S(maxCap)
                .currentFlowM3S(currentFlow)
                .plannedFlowM3S(plannedFlow)
                .remainingCapacityM3S(remainingCap)
                .utilizationPercent(utilizationPercent)
                .isOverloaded(isOverloaded)
                .constraintStatus(constraintStatus)
                .build();
    }

    public List<HydraulicConstraint> evaluatePathConstraints(
            List<WaterNetworkEdge> pathEdges,
            BigDecimal requiredFlowM3S,
            Map<UUID, Boolean> pipeBurstFlags,
            Map<UUID, Boolean> pressureDropFlags
    ) {
        List<HydraulicConstraint> constraints = new ArrayList<>();

        for (WaterNetworkEdge edge : pathEdges) {
            boolean pipeBurst = pipeBurstFlags != null && Boolean.TRUE.equals(pipeBurstFlags.get(edge.getId()));
            boolean pressureDrop = pressureDropFlags != null && Boolean.TRUE.equals(pressureDropFlags.get(edge.getId()));

            if (pipeBurst || pressureDrop) {
                constraints.add(HydraulicConstraint.builder()
                        .segmentId(edge.getId())
                        .segmentName(edge.getName())
                        .constraintType("HYDRAULIC_SAFETY_INTERLOCK")
                        .status("BLOCKED")
                        .message("Critical hydraulic anomaly active on " + edge.getName() + ". Pressurization prohibited.")
                        .maxAllowableFlowM3S(BigDecimal.ZERO)
                        .currentPlannedFlowM3S(requiredFlowM3S)
                        .isInterlockActive(true)
                        .build());
            } else if (requiredFlowM3S.compareTo(edge.getMaxCapacityM3S()) > 0) {
                constraints.add(HydraulicConstraint.builder()
                        .segmentId(edge.getId())
                        .segmentName(edge.getName())
                        .constraintType("CAPACITY_EXCEEDED")
                        .status("CONSTRAINED")
                        .message("Planned delivery flow (" + requiredFlowM3S + " m³/s) exceeds carrying capacity (" + edge.getMaxCapacityM3S() + " m³/s).")
                        .maxAllowableFlowM3S(edge.getMaxCapacityM3S())
                        .currentPlannedFlowM3S(requiredFlowM3S)
                        .isInterlockActive(false)
                        .build());
            } else {
                constraints.add(HydraulicConstraint.builder()
                        .segmentId(edge.getId())
                        .segmentName(edge.getName())
                        .constraintType("CAPACITY_SATISFIED")
                        .status("SAFE")
                        .message("Segment capacity nominal.")
                        .maxAllowableFlowM3S(edge.getMaxCapacityM3S())
                        .currentPlannedFlowM3S(requiredFlowM3S)
                        .isInterlockActive(false)
                        .build());
            }
        }

        return constraints;
    }
}
