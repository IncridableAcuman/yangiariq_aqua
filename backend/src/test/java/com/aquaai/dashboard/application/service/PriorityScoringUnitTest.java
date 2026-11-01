package com.aquaai.dashboard.application.service;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityScoringUnitTest {

    private PriorityScoringService scoringService;
    private Field testField;

    @BeforeEach
    void setUp() {
        scoringService = new PriorityScoringService();
        testField = Field.create(
                UUID.randomUUID(),
                "Dala 12 Paxta",
                "Test parcel",
                new BigDecimal("12.5"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                null,
                null
        );
    }

    @Test
    @DisplayName("Hydraulic safety interlock block strictly yields CRITICAL priority and safety investigation action")
    void hydraulicSafetyBlockYieldsCriticalPriority() {
        PriorityScoringService.ScoringResult result = scoringService.evaluateFieldPriority(
                testField,
                new BigDecimal("65.0"), // normal moisture
                new BigDecimal("60.0"),
                "FRESH",
                new BigDecimal("5000.0"),
                true, // hydraulic safety blocked
                "Pipe burst detected on mainline canal",
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        assertThat(result.priority()).isEqualTo(IrrigationPriority.CRITICAL);
        assertThat(result.score()).isEqualTo(100);
        assertThat(result.recommendedAction()).isEqualTo("INVESTIGATE_SAFETY");
        assertThat(result.actionTargetSection()).isEqualTo("anomalies");
        assertThat(result.reasons()).anyMatch(r -> r.contains("Hydraulic safety interlock"));
    }

    @Test
    @DisplayName("Critical drought (< 15% wilting threshold) produces CRITICAL priority and urgent action")
    void criticalDroughtYieldsCriticalPriority() {
        PriorityScoringService.ScoringResult result = scoringService.evaluateFieldPriority(
                testField,
                new BigDecimal("12.4"), // < 15%
                new BigDecimal("60.0"),
                "FRESH",
                new BigDecimal("3000.0"),
                false,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        assertThat(result.priority()).isEqualTo(IrrigationPriority.CRITICAL);
        assertThat(result.score()).isGreaterThanOrEqualTo(80);
        assertThat(result.recommendedAction()).isEqualTo("GENERATE_PLAN");
        assertThat(result.actionTargetSection()).isEqualTo("irrigation");
        assertThat(result.reasons()).anyMatch(r -> r.contains("wilting point"));
    }

    @Test
    @DisplayName("Proposed smart plan awaiting approval yields APPROVE_PLAN action")
    void proposedPlanYieldsApproveAction() {
        Instant now = Instant.now();
        SmartIrrigationPlan plan = SmartIrrigationPlan.createDraft(
                testField.getId(),
                UUID.randomUUID(),
                IrrigationPriority.HIGH,
                LocalDate.now().plusDays(1),
                now.plusSeconds(3600),
                now.plusSeconds(14400),
                new BigDecimal("450.0"),
                180,
                new BigDecimal("50.0"),
                new BigDecimal("60.0"),
                new BigDecimal("45.0"),
                "Deficit test reason",
                "Summary test",
                null,
                null
        );
        plan.propose();

        PriorityScoringService.ScoringResult result = scoringService.evaluateFieldPriority(
                testField,
                new BigDecimal("45.0"),
                new BigDecimal("60.0"),
                "FRESH",
                new BigDecimal("2000.0"),
                false,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                plan
        );

        assertThat(result.recommendedAction()).isEqualTo("APPROVE_PLAN");
        assertThat(result.actionTargetSection()).isEqualTo("irrigation");
        assertThat(result.reasons()).anyMatch(r -> r.contains("awaiting"));
    }

    @Test
    @DisplayName("Water quota depleted with moisture deficit yields REVIEW_WATER_ALLOCATION")
    void depletedWaterQuotaYieldsReviewAllocation() {
        PriorityScoringService.ScoringResult result = scoringService.evaluateFieldPriority(
                testField,
                new BigDecimal("22.0"), // deficient
                new BigDecimal("60.0"),
                "FRESH",
                BigDecimal.ZERO, // quota depleted
                false,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        assertThat(result.recommendedAction()).isEqualTo("REVIEW_WATER_ALLOCATION");
        assertThat(result.actionTargetSection()).isEqualTo("irrigation");
        assertThat(result.reasons()).anyMatch(r -> r.contains("depleted"));
    }

    @Test
    @DisplayName("Stale telemetry with no immediate crisis triggers sensor inspection")
    void staleTelemetryTriggersSensorInspection() {
        PriorityScoringService.ScoringResult result = scoringService.evaluateFieldPriority(
                testField,
                new BigDecimal("55.0"),
                new BigDecimal("60.0"),
                "STALE",
                new BigDecimal("4000.0"),
                false,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        assertThat(result.recommendedAction()).isEqualTo("INSPECT_SENSOR");
        assertThat(result.actionTargetSection()).isEqualTo("sensors");
        assertThat(result.reasons()).anyMatch(r -> r.contains("stale"));
    }

    @Test
    @DisplayName("Normal operational parameters yield LOW priority and MONITOR action")
    void normalParametersYieldLowPriority() {
        PriorityScoringService.ScoringResult result = scoringService.evaluateFieldPriority(
                testField,
                new BigDecimal("62.0"),
                new BigDecimal("60.0"),
                "FRESH",
                new BigDecimal("5000.0"),
                false,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        assertThat(result.priority()).isEqualTo(IrrigationPriority.LOW);
        assertThat(result.recommendedAction()).isEqualTo("MONITOR");
        assertThat(result.actionTargetSection()).isEqualTo("fields");
        assertThat(result.reasons()).anyMatch(r -> r.contains("optimal"));
    }
}
