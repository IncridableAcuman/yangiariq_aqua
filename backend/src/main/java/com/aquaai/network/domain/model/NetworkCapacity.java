package com.aquaai.network.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deterministic capacity profile for a canal segment or pipeline edge.
 */
@Builder
public record NetworkCapacity(
        UUID segmentId,
        String segmentName,
        WaterNetworkEdgeType edgeType,
        BigDecimal maxCapacityM3S,
        BigDecimal currentFlowM3S,
        BigDecimal plannedFlowM3S,
        BigDecimal remainingCapacityM3S,
        BigDecimal utilizationPercent,
        boolean isOverloaded,
        String constraintStatus // SAFE, CONSTRAINED, BLOCKED, UNKNOWN, INSUFFICIENT_DATA
) {
}
