package com.aquaai.network.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Constraint representation for a network conveyance segment.
 */
@Builder
public record HydraulicConstraint(
        UUID segmentId,
        String segmentName,
        String constraintType,
        String status, // SAFE, CONSTRAINED, BLOCKED, UNKNOWN, INSUFFICIENT_DATA
        String message,
        BigDecimal maxAllowableFlowM3S,
        BigDecimal currentPlannedFlowM3S,
        boolean isInterlockActive
) {
}
