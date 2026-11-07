package com.aquaai.network.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecordActualDeliveryRequest(
        UUID dispatchScheduleId,
        @NotNull UUID fieldId,
        String fieldName,
        UUID edgeId,
        String canalName,
        @NotNull Instant actualStartAt,
        @NotNull Instant actualEndAt,
        BigDecimal plannedVolumeM3,
        @NotNull BigDecimal actualVolumeM3,
        BigDecimal measuredFlowM3S,
        String operatorNotes
) {}
