package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Deterministic network topology validator.
 * Detects disconnected nodes, missing upstream connections, invalid paths,
 * and capacity anomalies across the regional distribution graph.
 */
public class NetworkTopologyValidator {

    public record TopologyValidationResult(
            boolean isValid,
            List<String> errors,
            List<String> warnings
    ) {
        public static TopologyValidationResult success() {
            return new TopologyValidationResult(true, List.of(), List.of());
        }
    }

    public TopologyValidationResult validate(List<WaterNetworkNode> nodes, List<WaterNetworkEdge> edges) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (nodes == null || nodes.isEmpty()) {
            return new TopologyValidationResult(false, List.of("Water network contains zero nodes."), List.of());
        }

        Map<UUID, WaterNetworkNode> nodeMap = new HashMap<>();
        Map<UUID, List<WaterNetworkEdge>> incomingEdges = new HashMap<>();
        Map<UUID, List<WaterNetworkEdge>> outgoingEdges = new HashMap<>();

        for (WaterNetworkNode node : nodes) {
            nodeMap.put(node.getId(), node);
            incomingEdges.put(node.getId(), new ArrayList<>());
            outgoingEdges.put(node.getId(), new ArrayList<>());
        }

        if (edges != null) {
            for (WaterNetworkEdge edge : edges) {
                if (!nodeMap.containsKey(edge.getStartNodeId())) {
                    errors.add("Edge '" + edge.getName() + "' references non-existent start node: " + edge.getStartNodeId());
                } else {
                    outgoingEdges.get(edge.getStartNodeId()).add(edge);
                }

                if (!nodeMap.containsKey(edge.getEndNodeId())) {
                    errors.add("Edge '" + edge.getName() + "' references non-existent end node: " + edge.getEndNodeId());
                } else {
                    incomingEdges.get(edge.getEndNodeId()).add(edge);
                }

                if (edge.getStartNodeId().equals(edge.getEndNodeId())) {
                    errors.add("Self-loop detected on edge '" + edge.getName() + "': start and end node are identical.");
                }

                if (edge.getMaxCapacityM3S().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    errors.add("Edge '" + edge.getName() + "' has non-positive carrying capacity: " + edge.getMaxCapacityM3S() + " m³/s.");
                }
            }
        }

        // Validate node connectivity
        Set<UUID> sourceNodeIds = new HashSet<>();
        for (WaterNetworkNode node : nodes) {
            if (node.getType() == WaterNetworkNodeType.WATER_SOURCE || node.getType() == WaterNetworkNodeType.RESERVOIR) {
                sourceNodeIds.add(node.getId());
            }

            int inCount = incomingEdges.get(node.getId()).size();
            int outCount = outgoingEdges.get(node.getId()).size();

            // Disconnected node check
            if (inCount == 0 && outCount == 0) {
                errors.add("Isolated disconnected node detected: '" + node.getName() + "' (Code: " + node.getCode() + ").");
            }

            // Downstream nodes must have incoming water supply
            if (node.getType() == WaterNetworkNodeType.FIELD_INLET
                    || node.getType() == WaterNetworkNodeType.DISTRIBUTION_POINT
                    || node.getType() == WaterNetworkNodeType.CANAL_JUNCTION
                    || node.getType() == WaterNetworkNodeType.PUMP_STATION) {
                if (inCount == 0) {
                    errors.add("Node '" + node.getName() + "' of type " + node.getType() + " has zero incoming water conveyance edges.");
                }
            }
        }

        if (sourceNodeIds.isEmpty()) {
            errors.add("Network contains zero WATER_SOURCE or RESERVOIR nodes. No water origination point available.");
        }

        // Reachability validation: check if every FIELD_INLET can reach at least one source
        for (WaterNetworkNode node : nodes) {
            if (node.getType() == WaterNetworkNodeType.FIELD_INLET) {
                boolean canReachSource = canTraverseBackToSource(node.getId(), sourceNodeIds, incomingEdges, new HashSet<>());
                if (!canReachSource) {
                    warnings.add("Field inlet '" + node.getName() + "' has no valid path connecting back to any water source.");
                }
            }
        }

        return new TopologyValidationResult(errors.isEmpty(), errors, warnings);
    }

    private boolean canTraverseBackToSource(
            UUID currentNodeId,
            Set<UUID> sourceNodeIds,
            Map<UUID, List<WaterNetworkEdge>> incomingEdges,
            Set<UUID> visited
    ) {
        if (sourceNodeIds.contains(currentNodeId)) {
            return true;
        }
        if (visited.contains(currentNodeId)) {
            return false;
        }
        visited.add(currentNodeId);

        List<WaterNetworkEdge> incoming = incomingEdges.get(currentNodeId);
        if (incoming == null || incoming.isEmpty()) {
            return false;
        }

        for (WaterNetworkEdge edge : incoming) {
            if (canTraverseBackToSource(edge.getStartNodeId(), sourceNodeIds, incomingEdges, visited)) {
                return true;
            }
        }
        return false;
    }
}
