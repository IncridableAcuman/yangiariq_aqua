package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.DataQualityGate;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class IrrigationPriorityCalculatorTest {

    private IrrigationPriorityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IrrigationPriorityCalculator();
    }

    @Test
    @DisplayName("Should assign CRITICAL or HIGH priority for severe moisture deficit on high-sensitivity crop")
    void testSevereMoistureDeficitPriority() {
        var result = calculator.calculatePriority(
                BigDecimal.valueOf(35.0),
                BigDecimal.valueOf(70.0),
                "COTTON",
                true,
                BigDecimal.valueOf(39.0),
                BigDecimal.ZERO,
                BigDecimal.valueOf(500.0),
                BigDecimal.valueOf(100.0),
                DataQualityGate.SUFFICIENT
        );

        assertThat(result.priority()).isIn(IrrigationPriority.CRITICAL, IrrigationPriority.HIGH);
        assertThat(result.score()).isGreaterThanOrEqualTo(60);
    }

    @Test
    @DisplayName("Should cap priority at MEDIUM if telemetry quality is INSUFFICIENT or UNSAFE")
    void testQualityGateCapsPriority() {
        var result = calculator.calculatePriority(
                BigDecimal.valueOf(20.0),
                BigDecimal.valueOf(70.0),
                "COTTON",
                true,
                BigDecimal.valueOf(40.0),
                BigDecimal.ZERO,
                BigDecimal.valueOf(500.0),
                BigDecimal.valueOf(100.0),
                DataQualityGate.INSUFFICIENT
        );

        assertThat(result.score()).isLessThanOrEqualTo(50);
        assertThat(result.priority()).isIn(IrrigationPriority.MEDIUM, IrrigationPriority.LOW);
        assertThat(result.reasoning()).contains("capped due to degraded/unreliable telemetry");
    }

    @Test
    @DisplayName("Should reduce priority when heavy rain is imminent")
    void testRainReducesPriority() {
        var dry = calculator.calculatePriority(
                BigDecimal.valueOf(55.0), BigDecimal.valueOf(70.0), "WHEAT", false,
                BigDecimal.valueOf(25.0), BigDecimal.ZERO, BigDecimal.valueOf(200.0),
                BigDecimal.valueOf(50.0), DataQualityGate.SUFFICIENT
        );

        var rain = calculator.calculatePriority(
                BigDecimal.valueOf(55.0), BigDecimal.valueOf(70.0), "WHEAT", false,
                BigDecimal.valueOf(25.0), BigDecimal.valueOf(25.0), BigDecimal.valueOf(200.0),
                BigDecimal.valueOf(50.0), DataQualityGate.SUFFICIENT
        );

        assertThat(rain.score()).isLessThan(dry.score());
    }
}
