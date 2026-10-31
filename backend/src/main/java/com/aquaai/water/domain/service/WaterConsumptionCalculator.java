package com.aquaai.water.domain.service;

import com.aquaai.water.domain.model.FieldWaterConsumption;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.PlannedVsActualMetrics;
import com.aquaai.water.domain.model.WaterConsumptionMetrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain service executing deterministic business formulas for water quota consumption,
 * remaining balances, over-consumption thresholds, and planned vs actual delivery variances.
 * <p>
 * Strictly free of Spring annotations, ORM frameworks, or infrastructure dependencies.
 */
public final class WaterConsumptionCalculator {

    public static final int VOLUME_SCALE = 4;
    public static final int PERCENTAGE_SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final BigDecimal ZERO_VOLUME = BigDecimal.ZERO.setScale(VOLUME_SCALE, ROUNDING_MODE);
    public static final BigDecimal ZERO_PERCENT = BigDecimal.ZERO.setScale(PERCENTAGE_SCALE, ROUNDING_MODE);

    private WaterConsumptionCalculator() {
        // Utility / domain service non-instantiable
    }

    /**
     * Calculates volumetric quota consumption, remaining balance, and over-consumption indicators.
     *
     * @param allocatedVolumeM3 Total quota assigned to the allocation
     * @param consumedVolumeM3  Total measured volume consumed across completed events
     * @return Immutable {@link WaterConsumptionMetrics}
     */
    public static WaterConsumptionMetrics calculateAllocationConsumption(
            BigDecimal allocatedVolumeM3,
            BigDecimal consumedVolumeM3
    ) {
        BigDecimal safeAllocated = normalizeVolume(allocatedVolumeM3);
        BigDecimal safeConsumed = normalizeVolume(consumedVolumeM3);

        boolean isOverConsumed = safeConsumed.compareTo(safeAllocated) > 0;

        // remaining = max(allocated - consumed, 0) -> strictly never negative
        BigDecimal remainingVolumeM3 = isOverConsumed
                ? ZERO_VOLUME
                : safeAllocated.subtract(safeConsumed).setScale(VOLUME_SCALE, ROUNDING_MODE);

        // overConsumedVolume = max(consumed - allocated, 0)
        BigDecimal overConsumedVolumeM3 = isOverConsumed
                ? safeConsumed.subtract(safeAllocated).setScale(VOLUME_SCALE, ROUNDING_MODE)
                : ZERO_VOLUME;

        // consumptionPercentage = consumed / allocated * 100 (guarded against zero division)
        BigDecimal consumptionPercentage = calculatePercentage(safeConsumed, safeAllocated);

        return new WaterConsumptionMetrics(
                safeAllocated,
                safeConsumed,
                remainingVolumeM3,
                consumptionPercentage,
                isOverConsumed,
                overConsumedVolumeM3
        );
    }

    /**
     * Calculates comparison metrics between planned irrigation schedules and executed water delivery.
     *
     * @param plannedVolumeM3 Scheduled target volume
     * @param actualVolumeM3  Measured actual water delivered
     * @return Immutable {@link PlannedVsActualMetrics}
     */
    public static PlannedVsActualMetrics calculatePlannedVsActual(
            BigDecimal plannedVolumeM3,
            BigDecimal actualVolumeM3
    ) {
        BigDecimal safePlanned = normalizeVolume(plannedVolumeM3);
        BigDecimal safeActual = normalizeVolume(actualVolumeM3);

        // variance = actual - planned
        BigDecimal varianceVolumeM3 = safeActual.subtract(safePlanned).setScale(VOLUME_SCALE, ROUNDING_MODE);

        // absolute variance = |actual - planned|
        BigDecimal absoluteVarianceVolumeM3 = varianceVolumeM3.abs();

        // variancePercentage = (actual - planned) / planned * 100
        BigDecimal variancePercentage = calculatePercentage(varianceVolumeM3, safePlanned);

        // consumptionRatioPercentage = actual / planned * 100
        BigDecimal consumptionRatioPercentage = calculatePercentage(safeActual, safePlanned);

        return new PlannedVsActualMetrics(
                safePlanned,
                safeActual,
                varianceVolumeM3,
                absoluteVarianceVolumeM3,
                variancePercentage,
                consumptionRatioPercentage
        );
    }

    /**
     * Derives parcel field-level aggregated water consumption metrics.
     *
     * @param fieldId Field aggregate identifier
     * @param from    Filter start timestamp
     * @param to      Filter end timestamp
     * @param rawData Aggregated totals from repository query
     * @return Immutable {@link FieldWaterConsumption}
     */
    public static FieldWaterConsumption calculateFieldConsumption(
            UUID fieldId,
            Instant from,
            Instant to,
            FieldWaterSummaryData rawData
    ) {
        BigDecimal safeAllocated = rawData != null ? normalizeVolume(rawData.totalAllocatedVolumeM3()) : ZERO_VOLUME;
        BigDecimal safeConsumed = rawData != null ? normalizeVolume(rawData.totalConsumedVolumeM3()) : ZERO_VOLUME;
        long allocationCount = rawData != null ? Math.max(rawData.allocationCount(), 0) : 0;
        long eventCount = rawData != null ? Math.max(rawData.irrigationEventCount(), 0) : 0;

        boolean isOverConsumed = safeConsumed.compareTo(safeAllocated) > 0;

        BigDecimal remainingVolumeM3 = isOverConsumed
                ? ZERO_VOLUME
                : safeAllocated.subtract(safeConsumed).setScale(VOLUME_SCALE, ROUNDING_MODE);

        BigDecimal overConsumedVolumeM3 = isOverConsumed
                ? safeConsumed.subtract(safeAllocated).setScale(VOLUME_SCALE, ROUNDING_MODE)
                : ZERO_VOLUME;

        BigDecimal consumptionPercentage = calculatePercentage(safeConsumed, safeAllocated);

        return new FieldWaterConsumption(
                fieldId,
                from,
                to,
                safeAllocated,
                safeConsumed,
                remainingVolumeM3,
                overConsumedVolumeM3,
                isOverConsumed,
                consumptionPercentage,
                allocationCount,
                eventCount
        );
    }

    private static BigDecimal normalizeVolume(BigDecimal volume) {
        if (volume == null || volume.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO_VOLUME;
        }
        return volume.setScale(VOLUME_SCALE, ROUNDING_MODE);
    }

    private static BigDecimal calculatePercentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO_PERCENT;
        }
        return numerator.multiply(ONE_HUNDRED)
                .divide(denominator, PERCENTAGE_SCALE, ROUNDING_MODE);
    }
}
