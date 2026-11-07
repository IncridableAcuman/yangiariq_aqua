package com.aquaai.network.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Operational surge assessment for pump station ramping and sudden gate operations.
 */
@Builder
public record HydraulicSurgeRisk(
        UUID segmentId,
        String segmentName,
        String riskStatus, // SAFE, CONSTRAINED, BLOCKED, UNKNOWN, INSUFFICIENT_DATA
        BigDecimal surgeIndexScore,
        boolean abruptChangeDetected,
        BigDecimal maxPermissibleRampingRateM3SPerHour,
        String recommendedAction,
        String explanation
) {
}
