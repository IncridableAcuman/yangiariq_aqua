package com.aquaai.network.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ActualDeliveryResponse(
        UUID id,
        UUID dispatchScheduleId,
        UUID fieldId,
        String fieldName,
        UUID edgeId,
        String canalName,
        Instant actualStartAt,
        Instant actualEndAt,
        BigDecimal plannedVolumeM3,
        BigDecimal actualVolumeM3,
        BigDecimal varianceVolumeM3,
        BigDecimal variancePercent,
        BigDecimal measuredFlowM3S,
        UUID operatorId,
        String operatorNotes,
        Instant createdAt
) {}
