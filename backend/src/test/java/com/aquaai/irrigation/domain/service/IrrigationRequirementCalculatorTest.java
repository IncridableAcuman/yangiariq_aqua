package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.IrrigationRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class IrrigationRequirementCalculatorTest {

    private IrrigationRequirementCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IrrigationRequirementCalculator();
    }

    @Test
    @DisplayName("Should calculate positive gross irrigation requirement when soil moisture is below target")
    void testLowSoilMoistureCalculation() {
        // Cotton, 2.0 ha, current moisture 40%, target 70%, ET0 6 mm/day, 0 rain, Drip (90% efficiency)
        IrrigationRequirement result = calculator.calculate(
                BigDecimal.valueOf(40.0),
                BigDecimal.valueOf(70.0),
                "COTTON",
                "LOAM",
                "DRIP",
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(6.0),
                BigDecimal.ZERO
        );

        assertThat(result.getDeficitDepthMm()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getNetRequirementMm()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getGrossVolumeM3()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getEstimatedDurationMinutes()).isGreaterThan(0);
        assertThat(result.getIrrigationEfficiency()).isEqualByComparingTo(BigDecimal.valueOf(0.90));
    }

    @Test
    @DisplayName("Should offset requirement when substantial rainfall is forecast")
    void testRainfallOffset() {
        // Without rain
        IrrigationRequirement dryResult = calculator.calculate(
                BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(70.0),
                "WHEAT",
                "LOAM",
                "SPRINKLER",
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.ZERO
        );

        // With 30 mm rain forecast
        IrrigationRequirement rainResult = calculator.calculate(
                BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(70.0),
                "WHEAT",
                "LOAM",
                "SPRINKLER",
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(30.0)
        );

        assertThat(rainResult.getNetRequirementMm()).isLessThan(dryResult.getNetRequirementMm());
        assertThat(rainResult.getGrossVolumeM3()).isLessThan(dryResult.getGrossVolumeM3());
    }

    @Test
    @DisplayName("Should scale gross volume based on irrigation method efficiency (Drip > Furrow > Canal)")
    void testEfficiencyScaling() {
        IrrigationRequirement drip = calculator.calculate(
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(70.0), "COTTON", "LOAM", "DRIP",
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(5.0), BigDecimal.ZERO
        );

        IrrigationRequirement furrow = calculator.calculate(
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(70.0), "COTTON", "LOAM", "FURROW",
                BigDecimal.valueOf(1.0), BigDecimal.valueOf(5.0), BigDecimal.ZERO
        );

        // Drip (90% eff) needs less gross water than Furrow (60% eff) for same net crop requirement
        assertThat(drip.getGrossVolumeM3()).isLessThan(furrow.getGrossVolumeM3());
    }

    @Test
    @DisplayName("Should produce 0 gross requirement if soil moisture is already above target and rain exceeds ETc")
    void testSufficientMoistureNoRequirement() {
        IrrigationRequirement result = calculator.calculate(
                BigDecimal.valueOf(80.0),
                BigDecimal.valueOf(70.0),
                "MAIZE",
                "CLAY",
                "DRIP",
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(20.0)
        );

        assertThat(result.getGrossVolumeM3()).isEqualTo(BigDecimal.ZERO.setScale(4));
        assertThat(result.getEstimatedDurationMinutes()).isEqualTo(0);
    }
}
