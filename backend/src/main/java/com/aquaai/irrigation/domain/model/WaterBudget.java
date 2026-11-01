package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing the seasonal or periodic volumetric water budget for an agricultural parcel.
 * Formula:
 * Remaining Available = Total Allocated - Consumed - Committed - Reserved
 */
@Getter
@Builder
public class WaterBudget {

    private final UUID fieldId;
    private final BigDecimal totalAllocatedM3;
    private final BigDecimal consumedM3;
    private final BigDecimal committedM3;
    private final BigDecimal reservedM3;
    private final BigDecimal remainingAvailableM3;

    public WaterBudget(
            UUID fieldId,
            BigDecimal totalAllocatedM3,
            BigDecimal consumedM3,
            BigDecimal committedM3,
            BigDecimal reservedM3,
            BigDecimal remainingAvailableM3
    ) {
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.totalAllocatedM3 = defaultZero(totalAllocatedM3);
        this.consumedM3 = defaultZero(consumedM3);
        this.committedM3 = defaultZero(committedM3);
        this.reservedM3 = defaultZero(reservedM3);

        BigDecimal calculatedRemaining = this.totalAllocatedM3
                .subtract(this.consumedM3)
                .subtract(this.committedM3)
                .subtract(this.reservedM3);

        this.remainingAvailableM3 = calculatedRemaining.compareTo(BigDecimal.ZERO) > 0
                ? calculatedRemaining.setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    public static WaterBudget calculate(
            UUID fieldId,
            BigDecimal totalAllocatedM3,
            BigDecimal consumedM3,
            BigDecimal committedM3,
            BigDecimal reservedM3
    ) {
        return new WaterBudget(fieldId, totalAllocatedM3, consumedM3, committedM3, reservedM3, null);
    }

    public boolean canFulfill(BigDecimal requiredVolumeM3) {
        if (requiredVolumeM3 == null || requiredVolumeM3.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return this.remainingAvailableM3.compareTo(requiredVolumeM3) >= 0;
    }

    public BigDecimal calculateShortage(BigDecimal requiredVolumeM3) {
        if (requiredVolumeM3 == null || canFulfill(requiredVolumeM3)) {
            return BigDecimal.ZERO;
        }
        return requiredVolumeM3.subtract(this.remainingAvailableM3).setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
