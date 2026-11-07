package com.aquaai.network.api.dto;

import com.aquaai.field.domain.model.CoordinatePoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WaterNetworkEdgeResponse(
        UUID id,
        String name,
        String code,
        String edgeType,
        UUID startNodeId,
        UUID endNodeId,
        UUID wuaId,
        BigDecimal lengthMeters,
        BigDecimal maxCapacityM3S,
        BigDecimal minFlowM3S,
        BigDecimal currentFlowM3S,
        String operationalStatus,
        BigDecimal utilizationPercent,
        List<CoordinatePoint> geometry
) {}
