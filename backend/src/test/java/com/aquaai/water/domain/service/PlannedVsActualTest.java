package com.aquaai.water.domain.service;

import com.aquaai.water.domain.model.PlannedVsActualMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PlannedVsActualTest {

    @Test
    @DisplayName("1. Actual < Planned: negative variance (water saved), positive absolute variance, ratio < 100%")
    void testActualLessThanPlanned() {
        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                new BigDecimal("1000.0000"),
                new BigDecimal("800.0000")
        );

        // variance = 800 - 1000 = -200
        assertThat(metrics.varianceVolumeM3()).isEqualByComparingTo("-200.0000");
        assertThat(metrics.absoluteVarianceVolumeM3()).isEqualByComparingTo("200.0000");
        // variance % = -200 / 1000 * 100 = -20.00%
        assertThat(metrics.variancePercentage()).isEqualByComparingTo("-20.00");
        // consumption ratio = 800 / 1000 * 100 = 80.00%
        assertThat(metrics.consumptionRatioPercentage()).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("2. Actual == Planned: exact target match, zero variance, ratio 100.00%")
    void testActualEqualsPlanned() {
        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                new BigDecimal("500.0000"),
                new BigDecimal("500.0000")
        );

        assertThat(metrics.varianceVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(metrics.absoluteVarianceVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(metrics.variancePercentage()).isEqualByComparingTo("0.00");
        assertThat(metrics.consumptionRatioPercentage()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("3. Actual > Planned: positive variance (excess delivery), ratio > 100%")
    void testActualGreaterThanPlanned() {
        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                new BigDecimal("400.0000"),
                new BigDecimal("500.0000")
        );

        // variance = 500 - 400 = +100
        assertThat(metrics.varianceVolumeM3()).isEqualByComparingTo("100.0000");
        assertThat(metrics.absoluteVarianceVolumeM3()).isEqualByComparingTo("100.0000");
        // variance % = +100 / 400 * 100 = +25.00%
        assertThat(metrics.variancePercentage()).isEqualByComparingTo("25.00");
        // consumption ratio = 500 / 400 * 100 = 125.00%
        assertThat(metrics.consumptionRatioPercentage()).isEqualByComparingTo("125.00");
    }

    @Test
    @DisplayName("4. Positive variance: correct signs and values across fractional cubic meters")
    void testPositiveVarianceValues() {
        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                new BigDecimal("250.2500"),
                new BigDecimal("300.5000")
        );

        assertThat(metrics.varianceVolumeM3()).isEqualByComparingTo("50.2500");
        assertThat(metrics.varianceVolumeM3().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    }

    @Test
    @DisplayName("5. Negative variance: correct signed representation indicating water conservation")
    void testNegativeVarianceValues() {
        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                new BigDecimal("300.5000"),
                new BigDecimal("250.2500")
        );

        assertThat(metrics.varianceVolumeM3()).isEqualByComparingTo("-50.2500");
        assertThat(metrics.varianceVolumeM3().compareTo(BigDecimal.ZERO)).isLessThan(0);
        assertThat(metrics.absoluteVarianceVolumeM3()).isEqualByComparingTo("50.2500");
    }

    @Test
    @DisplayName("6. Variance percentage: precise decimal percentage formatting with HALF_UP")
    void testVariancePercentageFraction() {
        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                new BigDecimal("300.0000"),
                new BigDecimal("200.0000")
        );

        // (200 - 300) / 300 * 100 = -33.3333... -> -33.33%
        assertThat(metrics.variancePercentage()).isEqualByComparingTo("-33.33");
        // 200 / 300 * 100 = 66.6666... -> 66.67%
        assertThat(metrics.consumptionRatioPercentage()).isEqualByComparingTo("66.67");
    }

    @Test
    @DisplayName("7. Rounding: half-way rounding behavior operates consistently")
    void testRoundingBehavior() {
        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                new BigDecimal("8.0000"),
                new BigDecimal("9.2500")
        );

        // 9.25 - 8.0 = 1.25
        // 1.25 / 8.0 * 100 = 15.625 -> 15.63%
        assertThat(metrics.variancePercentage()).isEqualByComparingTo("15.63");
        assertThat(metrics.consumptionRatioPercentage()).isEqualByComparingTo("115.63");
    }

    @Test
    @DisplayName("8. Planned zero or null defensive protection: zero division gracefully avoided")
    void testPlannedZeroDefensiveHandling() {
        PlannedVsActualMetrics zeroPlanned = WaterConsumptionCalculator.calculatePlannedVsActual(
                BigDecimal.ZERO,
                new BigDecimal("200.0000")
        );

        assertThat(zeroPlanned.varianceVolumeM3()).isEqualByComparingTo("200.0000");
        assertThat(zeroPlanned.variancePercentage()).isEqualByComparingTo("0.00");
        assertThat(zeroPlanned.consumptionRatioPercentage()).isEqualByComparingTo("0.00");
    }
}
