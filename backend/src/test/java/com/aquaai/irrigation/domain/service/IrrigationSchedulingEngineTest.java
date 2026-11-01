package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.ConstraintStatus;
import com.aquaai.irrigation.domain.model.ConstraintType;
import com.aquaai.irrigation.domain.model.IrrigationRequirement;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IrrigationSchedulingEngineTest {

    private IrrigationSchedulingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new IrrigationSchedulingEngine(
                new HydraulicSafetyInterlock(),
                new IrrigationPriorityCalculator()
        );
    }

    @Test
    @DisplayName("Should detect water quota shortage and constrain planned volume to available quota")
    void testQuotaShortageScaling() {
        // Requirement: 200 m3
        IrrigationRequirement requirement = new IrrigationRequirement(
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(20.0),
                BigDecimal.valueOf(200.0),
                240,
                BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(0.75)
        );

        // Water budget with only 120 m3 remaining
        WaterBudget budget = WaterBudget.calculate(
                UUID.randomUUID(),
                BigDecimal.valueOf(1000.0),
                BigDecimal.valueOf(880.0),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        SmartIrrigationPlan plan = engine.schedulePlan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                requirement,
                budget,
                LocalDate.now(),
                ZoneId.of("Asia/Tashkent"),
                false,
                false,
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(40.0),
                BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(70.0),
                "COTTON",
                "DRIP",
                BigDecimal.valueOf(30.0),
                BigDecimal.ZERO,
                BigDecimal.valueOf(2.0)
        );

        // Volume capped at available budget
        assertThat(plan.getRecommendedVolumeM3()).isEqualByComparingTo(BigDecimal.valueOf(120.0));
        assertThat(plan.getConstraints()).anyMatch(c -> c.getType() == ConstraintType.WATER_QUOTA && c.getStatus() == ConstraintStatus.WARNING);
        assertThat(plan.getTradeoffs()).anyMatch(t -> t.getCategory().equals("WATER_QUOTA_ADJUSTMENT"));
    }

    @Test
    @DisplayName("Should schedule nocturnal window when ambient temperature is elevated (>32°C)")
    void testNocturnalWindowSelection() {
        IrrigationRequirement requirement = new IrrigationRequirement(
                BigDecimal.valueOf(20.0), BigDecimal.valueOf(15.0), BigDecimal.valueOf(100.0),
                120, BigDecimal.valueOf(50.0), BigDecimal.valueOf(0.75)
        );

        SmartIrrigationPlan plan = engine.schedulePlan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                requirement,
                null,
                LocalDate.now(),
                ZoneId.of("Asia/Tashkent"),
                false,
                false,
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(40.0),
                BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(70.0),
                "COTTON",
                "SPRINKLER",
                BigDecimal.valueOf(36.0), // High heat
                BigDecimal.ZERO,
                BigDecimal.valueOf(3.0)
        );

        // Should start at 22:00
        assertThat(plan.getPlannedStartAt().atZone(ZoneId.of("Asia/Tashkent")).getHour()).isEqualTo(22);
        assertThat(plan.getTradeoffs()).anyMatch(t -> t.getCategory().equals("NOCTURNAL_DISPATCH"));
    }
}
