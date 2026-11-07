package com.aquaai.network.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Concrete scheduled water delivery assigned to a specific parcel.
 */
@Builder
public record DispatchAssignment(
        UUID id,
        UUID fieldId,
        String fieldName,
        UUID wuaId,
        String wuaName,
        UUID canalSegmentId,
        String canalName,
        BigDecimal requestedVolumeM3,
        BigDecimal allocatedVolumeM3,
        Instant deliveryStartAt,
        Instant deliveryEndAt,
        BigDecimal flowRateM3S,
        int priorityRank,
        BigDecimal satisfactionRatePercent,
        String rationale
) {
}
