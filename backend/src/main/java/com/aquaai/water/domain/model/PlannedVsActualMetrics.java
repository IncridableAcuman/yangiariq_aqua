package com.aquaai.water.domain.model;

import java.math.BigDecimal;

/**
 * Immutable domain value object representing comparison metrics between planned irrigation
 * water and actual measured water delivery.
 *
 * @param plannedVolumeM3            Scheduled water volume in cubic meters (m³)
 * @param actualVolumeM3             Measured operational water volume delivered in cubic meters (m³)
 * @param varianceVolumeM3           Signed volumetric difference (actual - planned)
 * @param absoluteVarianceVolumeM3   Absolute magnitude of difference (|actual - planned|)
 * @param variancePercentage         Relative variance percentage ((actual - planned) / planned * 100)
 * @param consumptionRatioPercentage Operational consumption ratio (actual / planned * 100)
 */
public record PlannedVsActualMetrics(
        BigDecimal plannedVolumeM3,
        BigDecimal actualVolumeM3,
        BigDecimal varianceVolumeM3,
        BigDecimal absoluteVarianceVolumeM3,
        BigDecimal variancePercentage,
        BigDecimal consumptionRatioPercentage
) {
}
