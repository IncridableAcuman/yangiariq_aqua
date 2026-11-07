package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Traversal engine for path resolution, downstream dependency discovery,
 * and upstream bottleneck identification.
 */
public class PathResolutionEngine {

    public record ResolvedPath(
            List<WaterNetworkNode> nodes,
            List<WaterNetworkEdge> edges,
            BigDecimal bottleneckCapacityM3S,
            UUID bottleneckEdgeId,
            String bottleneckEdgeName
    ) {}

    /**
     * Finds all downstream field IDs that depend on water conveyed through a given canal segment/edge.
     */
    public List<UUID> findDownstreamFieldIds(
            UUID targetEdgeId,
            List<WaterNetworkNode> allNodes,
            List<WaterNetworkEdge> allEdges
    ) {
        WaterNetworkEdge targetEdge = allEdges.stream()
                .filter(e -> e.getId().equals(targetEdgeId))
                .findFirst()
                .orElse(null);

        if (targetEdge == null) {
            return Collections.emptyList();
        }

        Map<UUID, List<WaterNetworkEdge>> outgoingEdges = new HashMap<>();
        for (WaterNetworkEdge edge : allEdges) {
            outgoingEdges.computeIfAbsent(edge.getStartNodeId(), k -> new ArrayList<>()).add(edge);
        }

        Map<UUID, WaterNetworkNode> nodeMap = new HashMap<>();
        for (WaterNetworkNode node : allNodes) {
            nodeMap.put(node.getId(), node);
        }

        Set<UUID> downstreamFieldIds = new HashSet<>();
        Set<UUID> visitedNodes = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();

        queue.add(targetEdge.getEndNodeId());
        visitedNodes.add(targetEdge.getEndNodeId());

        while (!queue.isEmpty()) {
            UUID currentId = queue.poll();
            WaterNetworkNode node = nodeMap.get(currentId);
            if (node != null && node.getType() == WaterNetworkNodeType.FIELD_INLET && node.getFieldId() != null) {
                downstreamFieldIds.add(node.getFieldId());
            }

            List<WaterNetworkEdge> outgoing = outgoingEdges.getOrDefault(currentId, Collections.emptyList());
            for (WaterNetworkEdge edge : outgoing) {
                if (!visitedNodes.contains(edge.getEndNodeId())) {
                    visitedNodes.add(edge.getEndNodeId());
                    queue.add(edge.getEndNodeId());
                }
            }
        }

        return new ArrayList<>(downstreamFieldIds);
    }

    /**
     * Resolves the primary supply path from an upstream water source to a target field inlet node.
     */
    public ResolvedPath resolveSourceToFieldPath(
            UUID fieldNodeId,
            List<WaterNetworkNode> allNodes,
            List<WaterNetworkEdge> allEdges
    ) {
        Map<UUID, WaterNetworkNode> nodeMap = new HashMap<>();
        for (WaterNetworkNode n : allNodes) nodeMap.put(n.getId(), n);

        Map<UUID, List<WaterNetworkEdge>> incomingEdges = new HashMap<>();
        for (WaterNetworkEdge e : allEdges) {
            incomingEdges.computeIfAbsent(e.getEndNodeId(), k -> new ArrayList<>()).add(e);
        }

        List<WaterNetworkNode> pathNodes = new ArrayList<>();
        List<WaterNetworkEdge> pathEdges = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();

        UUID current = fieldNodeId;
        WaterNetworkNode currentNode = nodeMap.get(current);
        if (currentNode != null) {
            pathNodes.add(currentNode);
        }

        while (current != null && !visited.contains(current)) {
            visited.add(current);
            WaterNetworkNode node = nodeMap.get(current);
            if (node != null && (node.getType() == WaterNetworkNodeType.WATER_SOURCE || node.getType() == WaterNetworkNodeType.RESERVOIR)) {
                break; // Reached water origination point
            }

            List<WaterNetworkEdge> incoming = incomingEdges.getOrDefault(current, Collections.emptyList());
            if (incoming.isEmpty()) {
                break;
            }

            // Pick the primary supply edge (highest capacity if multiple options exist)
            WaterNetworkEdge supplyEdge = incoming.stream()
                    .max((a, b) -> a.getMaxCapacityM3S().compareTo(b.getMaxCapacityM3S()))
                    .orElse(incoming.get(0));

            pathEdges.add(supplyEdge);
            current = supplyEdge.getStartNodeId();
            WaterNetworkNode prevNode = nodeMap.get(current);
            if (prevNode != null) {
                pathNodes.add(prevNode);
            }
        }

        Collections.reverse(pathNodes);
        Collections.reverse(pathEdges);

        BigDecimal bottleneck = null;
        UUID bottleneckId = null;
        String bottleneckName = null;

        for (WaterNetworkEdge e : pathEdges) {
            if (bottleneck == null || e.getMaxCapacityM3S().compareTo(bottleneck) < 0) {
                bottleneck = e.getMaxCapacityM3S();
                bottleneckId = e.getId();
                bottleneckName = e.getName();
            }
        }

        return new ResolvedPath(
                pathNodes,
                pathEdges,
                bottleneck != null ? bottleneck : BigDecimal.ZERO,
                bottleneckId,
                bottleneckName
        );
    }
}
