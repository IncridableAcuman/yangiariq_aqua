package com.aquaai.network.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SegmentCapacityResponse(
        UUID edgeId,
        String edgeName,
        String edgeCode,
        String edgeType,
        BigDecimal maxCapacityM3S,
        BigDecimal currentFlowM3S,
        BigDecimal availableCapacityM3S,
        BigDecimal utilizationPercent,
        String status,
        boolean isBlocked,
        String blockReason,
        List<String> safetyConstraintViolations,
        int dependentFieldCount,
        List<String> dependentFieldNames
) {}
