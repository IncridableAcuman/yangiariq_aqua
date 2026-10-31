package com.aquaai.water.domain.service;

import com.aquaai.water.domain.model.WaterConsumptionMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WaterConsumptionCalculationTest {

    @Test
    @DisplayName("1. Allocated 1000 m³ / Consumed 0 m³: remaining 1000 m³, 0.00% utilization, no over-consumption")
    void testAllocated1000Consumed0() {
        WaterConsumptionMetrics metrics = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("1000.0000"),
                BigDecimal.ZERO
        );

        assertThat(metrics.allocatedVolumeM3()).isEqualByComparingTo("1000.0000");
        assertThat(metrics.consumedVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(metrics.remainingVolumeM3()).isEqualByComparingTo("1000.0000");
        assertThat(metrics.consumptionPercentage()).isEqualByComparingTo("0.00");
        assertThat(metrics.isOverConsumed()).isFalse();
        assertThat(metrics.overConsumedVolumeM3()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("2. Allocated 1000 m³ / Consumed 500 m³: remaining 500 m³, 50.00% utilization, no over-consumption")
    void testAllocated1000Consumed500() {
        WaterConsumptionMetrics metrics = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("1000.0000"),
                new BigDecimal("500.0000")
        );

        assertThat(metrics.remainingVolumeM3()).isEqualByComparingTo("500.0000");
        assertThat(metrics.consumptionPercentage()).isEqualByComparingTo("50.00");
        assertThat(metrics.isOverConsumed()).isFalse();
        assertThat(metrics.overConsumedVolumeM3()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("3. Allocated 1000 m³ / Consumed 1000 m³: remaining 0 m³, 100.00% utilization, exact quota reached")
    void testAllocated1000Consumed1000() {
        WaterConsumptionMetrics metrics = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("1000.0000"),
                new BigDecimal("1000.0000")
        );

        assertThat(metrics.remainingVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(metrics.consumptionPercentage()).isEqualByComparingTo("100.00");
        assertThat(metrics.isOverConsumed()).isFalse();
        assertThat(metrics.overConsumedVolumeM3()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("4. Allocated 1000 m³ / Consumed 1200 m³: remaining 0 m³, 120.00% utilization, over-consumption detected")
    void testAllocated1000Consumed1200() {
        WaterConsumptionMetrics metrics = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("1000.0000"),
                new BigDecimal("1200.0000")
        );

        assertThat(metrics.remainingVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(metrics.consumptionPercentage()).isEqualByComparingTo("120.00");
        assertThat(metrics.isOverConsumed()).isTrue();
        assertThat(metrics.overConsumedVolumeM3()).isEqualByComparingTo("200.0000");
    }

    @Test
    @DisplayName("5. Remaining volume invariant: remaining volume MUST NEVER be negative under any over-consumption magnitude")
    void testRemainingVolumeNeverNegative() {
        WaterConsumptionMetrics extremeOver = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("100.0000"),
                new BigDecimal("9999.0000")
        );

        assertThat(metricsRemainingNeverNegative(extremeOver)).isTrue();
        assertThat(extremeOver.remainingVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(extremeOver.isOverConsumed()).isTrue();
        assertThat(extremeOver.overConsumedVolumeM3()).isEqualByComparingTo("9899.0000");
    }

    @Test
    @DisplayName("6. Over-consumption amount: exactly calculates delta beyond assigned quota")
    void testOverConsumptionAmount() {
        WaterConsumptionMetrics metrics = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("750.5000"),
                new BigDecimal("825.7500")
        );

        assertThat(metrics.isOverConsumed()).isTrue();
        assertThat(metrics.overConsumedVolumeM3()).isEqualByComparingTo("75.2500");
    }

    @Test
    @DisplayName("7. Consumption percentage calculation: fractional percentages computed with precision")
    void testConsumptionPercentagePrecision() {
        WaterConsumptionMetrics metrics = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("300.0000"),
                new BigDecimal("100.0000")
        );

        // 100 / 300 * 100 = 33.33%
        assertThat(metrics.consumptionPercentage()).isEqualByComparingTo("33.33");
    }

    @Test
    @DisplayName("8. Zero division defensive protection: zero or non-positive allocated volume yields 0.00% without exception")
    void testZeroDivisionProtection() {
        WaterConsumptionMetrics zeroAllocated = WaterConsumptionCalculator.calculateAllocationConsumption(
                BigDecimal.ZERO,
                new BigDecimal("500.0000")
        );

        assertThat(zeroAllocated.consumptionPercentage()).isEqualByComparingTo("0.00");
        assertThat(zeroAllocated.isOverConsumed()).isTrue();
        assertThat(zeroAllocated.overConsumedVolumeM3()).isEqualByComparingTo("500.0000");
    }

    @Test
    @DisplayName("9. BigDecimal rounding: HALF_UP rounding applies correctly across decimal fractions")
    void testBigDecimalRounding() {
        WaterConsumptionMetrics roundUp = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("600.0000"),
                new BigDecimal("400.0000")
        );

        // 400 / 600 * 100 = 66.6666... -> 66.67%
        assertThat(roundUp.consumptionPercentage()).isEqualByComparingTo("66.67");
    }

    @Test
    @DisplayName("10. Null and negative input protection: nulls and negative values defensively handled as zero volume")
    void testNullAndNegativeInputProtection() {
        WaterConsumptionMetrics nullInputs = WaterConsumptionCalculator.calculateAllocationConsumption(null, null);

        assertThat(nullInputs.allocatedVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(nullInputs.consumedVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(nullInputs.remainingVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(nullInputs.consumptionPercentage()).isEqualByComparingTo("0.00");
        assertThat(nullInputs.isOverConsumed()).isFalse();

        WaterConsumptionMetrics negativeInputs = WaterConsumptionCalculator.calculateAllocationConsumption(
                new BigDecimal("-50.0000"),
                new BigDecimal("-10.0000")
        );

        assertThat(negativeInputs.allocatedVolumeM3()).isEqualByComparingTo("0.0000");
        assertThat(negativeInputs.consumedVolumeM3()).isEqualByComparingTo("0.0000");
    }

    private boolean metricsRemainingNeverNegative(WaterConsumptionMetrics m) {
        return m.remainingVolumeM3().compareTo(BigDecimal.ZERO) >= 0;
    }
}
