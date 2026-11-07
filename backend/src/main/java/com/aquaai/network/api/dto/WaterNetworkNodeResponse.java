package com.aquaai.network.api.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record WaterNetworkNodeResponse(
        UUID id,
        String name,
        String code,
        String nodeType,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal elevationMeters,
        UUID waterSourceId,
        UUID fieldId,
        UUID wuaId,
        boolean isActive,
        Map<String, Object> properties
) {}
