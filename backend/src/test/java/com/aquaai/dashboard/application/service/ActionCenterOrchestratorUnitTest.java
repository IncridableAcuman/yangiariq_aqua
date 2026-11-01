package com.aquaai.dashboard.application.service;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.dashboard.api.dto.PriorityFieldDto;
import com.aquaai.dashboard.api.dto.UrgentActionDto;
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
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
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

class ActionCenterOrchestratorUnitTest {

    private ActionCenterOrchestrator orchestrator;
    private Field field;

    @BeforeEach
    void setUp() {
        orchestrator = new ActionCenterOrchestrator();
        field = Field.create(
                UUID.randomUUID(),
                "Dala 04 Paxta",
                "Parcel desc",
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                null,
                null
        );
    }

    @Test
    @DisplayName("Prioritizes Hydraulic Safety Blocks at top severity CRITICAL")
    void hydraulicSafetyBlockHasTopPriority() {
        PriorityFieldDto pf = PriorityFieldDto.builder()
                .fieldId(field.getId())
                .fieldName(field.getName())
                .hydraulicSafetyBlocked(true)
                .hydraulicSafetyReason("Suspected mainline blowout")
                .priority(IrrigationPriority.CRITICAL)
                .build();

        Instant now = Instant.now();
        SmartIrrigationPlan plan = SmartIrrigationPlan.createDraft(
                field.getId(),
                UUID.randomUUID(),
                IrrigationPriority.HIGH,
                LocalDate.now(),
                now.plusSeconds(3600),
                now.plusSeconds(14400),
                new BigDecimal("300.0"),
                180,
                new BigDecimal("50.0"),
                new BigDecimal("60.0"),
                new BigDecimal("45.0"),
                "Hydraulic test plan",
                "Summary test",
                null,
                null
        );
        plan.propose();

        List<UrgentActionDto> actions = orchestrator.deriveUrgentActions(
                List.of(field),
                List.of(pf),
                List.of(plan),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertThat(actions).isNotEmpty();
        assertThat(actions.get(0).getActionType()).isEqualTo("HYDRAULIC_SAFETY_BLOCK");
        assertThat(actions.get(0).getSeverity()).isEqualTo("CRITICAL");
        assertThat(actions.get(0).getTargetSection()).isEqualTo("anomalies");
    }

    @Test
    @DisplayName("Sorts actions deterministically: CRITICAL before HIGH before MEDIUM")
    void sortsActionsBySeverity() {
        PriorityFieldDto pf = PriorityFieldDto.builder()
                .fieldId(field.getId())
                .fieldName(field.getName())
                .currentMoisturePercent(new BigDecimal("12.0")) // critical drought
                .priority(IrrigationPriority.CRITICAL)
                .build();

        Instant now = Instant.now();
        SmartIrrigationPlan plan = SmartIrrigationPlan.createDraft(
                field.getId(),
                UUID.randomUUID(),
                IrrigationPriority.HIGH,
                LocalDate.now(),
                now.plusSeconds(3600),
                now.plusSeconds(14400),
                new BigDecimal("350.0"),
                180,
                new BigDecimal("50.0"),
                new BigDecimal("60.0"),
                new BigDecimal("45.0"),
                "Priority sorting test plan",
                "Summary test",
                null,
                null
        );
        plan.propose();

        Sensor offlineSensor = Sensor.create(
                field.getId(),
                "Sensor 99",
                "SM-099",
                SensorType.SOIL_MOISTURE,
                "SIMULATED",
                "Offline moisture probe"
        );
        offlineSensor.markOffline();

        List<UrgentActionDto> actions = orchestrator.deriveUrgentActions(
                List.of(field),
                List.of(pf),
                List.of(plan),
                Collections.emptyList(),
                Collections.emptyList(),
                List.of(offlineSensor)
        );

        assertThat(actions).hasSize(3);
        assertThat(actions.get(0).getSeverity()).isEqualTo("CRITICAL");
        assertThat(actions.get(1).getSeverity()).isEqualTo("HIGH");
        assertThat(actions.get(2).getSeverity()).isEqualTo("MEDIUM");
    }
}
