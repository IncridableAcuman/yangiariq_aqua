package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.ConstraintStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HydraulicSafetyInterlockTest {

    private HydraulicSafetyInterlock interlock;

    @BeforeEach
    void setUp() {
        interlock = new HydraulicSafetyInterlock();
    }

    @Test
    @DisplayName("Should block irrigation when SUSPECTED_PIPE_BURST is active")
    void testSuspectedPipeBurstBlocksIrrigation() {
        var result = interlock.evaluate(true, false, BigDecimal.valueOf(1.8), BigDecimal.valueOf(45.0));

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.blockedReason()).contains("SUSPECTED_PIPE_BURST");
        assertThat(result.constraints()).anyMatch(c -> c.getStatus() == ConstraintStatus.BLOCKED);
    }

    @Test
    @DisplayName("Should block irrigation when critical pressure drop is detected")
    void testCriticalPressureDropBlocksIrrigation() {
        var result = interlock.evaluate(false, true, BigDecimal.valueOf(0.4), BigDecimal.valueOf(10.0));

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.blockedReason()).contains("Critical pressure drop");
        assertThat(result.constraints()).anyMatch(c -> c.getStatus() == ConstraintStatus.BLOCKED);
    }

    @Test
    @DisplayName("Should warn when pressure is low but not an active burst")
    void testLowPressureWarning() {
        var result = interlock.evaluate(false, false, BigDecimal.valueOf(0.8), BigDecimal.valueOf(25.0));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.constraints()).anyMatch(c -> c.getStatus() == ConstraintStatus.WARNING);
    }

    @Test
    @DisplayName("Should pass normally when hydraulic parameters are intact")
    void testNormalHydraulicOperation() {
        var result = interlock.evaluate(false, false, BigDecimal.valueOf(2.5), BigDecimal.valueOf(35.0));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.blockedReason()).isNull();
        assertThat(result.constraints()).allMatch(c -> c.getStatus() == ConstraintStatus.SATISFIED);
    }
}
