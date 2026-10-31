package com.aquaai.water.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain calculation result representing total periodic water consumption, remaining balance,
 * and over-consumption indicators for an agricultural field parcel.
 */
public record FieldWaterConsumption(
        UUID fieldId,
        Instant from,
        Instant to,
        BigDecimal totalAllocatedVolumeM3,
        BigDecimal totalConsumedVolumeM3,
        BigDecimal totalRemainingVolumeM3,
        BigDecimal totalOverConsumedVolumeM3,
        boolean isOverConsumed,
        BigDecimal consumptionPercentage,
        long allocationCount,
        long irrigationEventCount
) {
}
