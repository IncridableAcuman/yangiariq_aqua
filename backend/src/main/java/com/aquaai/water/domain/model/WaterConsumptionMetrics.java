package com.aquaai.water.domain.model;

import java.math.BigDecimal;

/**
 * Immutable domain value object representing calculated volumetric water consumption metrics
 * for a water allocation quota.
 *
 * @param allocatedVolumeM3    Total volumetric quota allocated in cubic meters (m³)
 * @param consumedVolumeM3     Total measured volumetric water delivered in cubic meters (m³)
 * @param remainingVolumeM3    Remaining available quota (clamped to 0, never negative)
 * @param consumptionPercentage Quota utilization percentage (consumed / allocated * 100)
 * @param isOverConsumed       True if actual consumption strictly exceeds the allocated quota
 * @param overConsumedVolumeM3 Volume consumed beyond the allocated quota (max(consumed - allocated, 0))
 */
public record WaterConsumptionMetrics(
        BigDecimal allocatedVolumeM3,
        BigDecimal consumedVolumeM3,
        BigDecimal remainingVolumeM3,
        BigDecimal consumptionPercentage,
        boolean isOverConsumed,
        BigDecimal overConsumedVolumeM3
) {
}
