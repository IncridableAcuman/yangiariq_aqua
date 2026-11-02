package com.aquaai.operations;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.operations.api.dto.ScheduleConflictDto;
import com.aquaai.operations.application.service.OperationalWorkflowDerivationService;
import com.aquaai.operations.domain.model.OperationalWorkflowState;
import com.aquaai.operations.domain.model.WorkflowStepStatus;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
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

class OperationalWorkflowDerivationServiceUnitTest {

    private OperationalWorkflowDerivationService derivationService;

    private UUID fieldId;
    private Instant now;

    @BeforeEach
    void setUp() {
        derivationService = new OperationalWorkflowDerivationService();
        fieldId = UUID.randomUUID();
        now = Instant.now();
    }

    @Test
    @DisplayName("Derives BLOCKED state when hydraulic safety interlock is active")
    void derivesBlockedStateWhenHydraulicInterlockIsActive() {
        var result = derivationService.derive(
                null,
                null,
                null,
                null,
                true,
                "Pipe burst suspected on distribution mainline",
                ScheduleConflictDto.builder().hasConflict(false).build()
        );

        assertThat(result.state()).isEqualTo(OperationalWorkflowState.BLOCKED);
        assertThat(result.isBlocked()).isTrue();
        assertThat(result.blockedReason()).contains("Pipe burst suspected");
        assertThat(result.availableActions()).contains("INVESTIGATE_SAFETY");

        // Approved step should be marked BLOCKED on timeline
        var approvedStep = result.timeline().stream()
                .filter(s -> "APPROVED".equals(s.getStepKey()))
                .findFirst()
                .orElseThrow();
        assertThat(approvedStep.getStatus()).isEqualTo(WorkflowStepStatus.BLOCKED);
    }

    @Test
    @DisplayName("Derives RECOMMENDATION_READY when active recommendation is GENERATED")
    void derivesRecommendationReadyState() {
        AgronomicRecommendation recommendation = AgronomicRecommendation.create(
                fieldId,
                RecommendationType.IRRIGATE_NOW,
                RecommendationPriority.HIGH,
                "Irrigate Cotton",
                "Root-zone soil moisture below threshold",
                "Hargreaves-Samani indicates high ET0",
                BigDecimal.valueOf(0.85),
                Instant.now(),
                null,
                com.aquaai.recommendation.domain.model.RecommendationSource.DETERMINISTIC_ENGINE,
                null,
                false,
                Collections.emptyList(),
                Collections.emptyList()
        );

        var result = derivationService.derive(
                null,
                recommendation,
                null,
                null,
                false,
                null,
                ScheduleConflictDto.builder().hasConflict(false).build()
        );

        assertThat(result.state()).isEqualTo(OperationalWorkflowState.RECOMMENDATION_READY);
        assertThat(result.availableActions()).contains("PREPARE_PLAN", "DISMISS_RECOMMENDATION");
    }

    @Test
    @DisplayName("Derives APPROVAL_REQUIRED when plan is in PROPOSED status")
    void derivesApprovalRequiredState() {
        SmartIrrigationPlan plan = SmartIrrigationPlan.createDraft(
                fieldId,
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                now.plusSeconds(3600),
                now.plusSeconds(7200),
                BigDecimal.valueOf(400),
                60,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(20),
                "Proposed plan",
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
        plan.propose();

        var result = derivationService.derive(
                null,
                null,
                plan,
                null,
                false,
                null,
                ScheduleConflictDto.builder().hasConflict(false).build()
        );

        assertThat(result.state()).isEqualTo(OperationalWorkflowState.APPROVAL_REQUIRED);
        assertThat(result.availableActions()).contains("APPROVE_PLAN", "REJECT_PLAN", "EDIT_PLAN");
    }

    @Test
    @DisplayName("Derives IN_PROGRESS and COMPLETE_OPERATION action when plan is actively executing")
    void derivesInProgressState() {
        SmartIrrigationPlan plan = SmartIrrigationPlan.createDraft(
                fieldId,
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                now.minusSeconds(1800),
                now.plusSeconds(1800),
                BigDecimal.valueOf(400),
                60,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(20),
                "In progress plan",
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
        plan.approve(UUID.randomUUID());
        plan.schedule();
        plan.start();

        var result = derivationService.derive(
                null,
                null,
                plan,
                null,
                false,
                null,
                ScheduleConflictDto.builder().hasConflict(false).build()
        );

        assertThat(result.state()).isEqualTo(OperationalWorkflowState.IN_PROGRESS);
        assertThat(result.availableActions()).contains("COMPLETE_OPERATION", "CANCEL_PLAN");
    }

    @Test
    @DisplayName("Derives VERIFIED when plan and event are completed")
    void derivesVerifiedStateWhenEventAndPlanAreCompleted() {
        SmartIrrigationPlan plan = SmartIrrigationPlan.createDraft(
                fieldId,
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                now.minusSeconds(7200),
                now.minusSeconds(3600),
                BigDecimal.valueOf(400),
                60,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(20),
                "Completed plan",
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
        plan.approve(UUID.randomUUID());
        plan.schedule();
        plan.start();
        plan.complete(BigDecimal.valueOf(410));

        IrrigationEvent event = IrrigationEvent.start(
                fieldId,
                plan.getId(),
                now.minusSeconds(7200),
                "Operator execution"
        );
        event.complete(now.minusSeconds(3600), BigDecimal.valueOf(410), "Turn completed successfully");

        var result = derivationService.derive(
                null,
                null,
                plan,
                event,
                false,
                null,
                ScheduleConflictDto.builder().hasConflict(false).build()
        );

        assertThat(result.state()).isEqualTo(OperationalWorkflowState.VERIFIED);
        assertThat(result.availableActions()).contains("VERIFY_RESULT", "NEW_CYCLE");
    }
}
