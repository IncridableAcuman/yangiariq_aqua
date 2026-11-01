package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmartIrrigationPlanLifecycleTest {

    private SmartIrrigationPlan createSamplePlan() {
        Instant now = Instant.now();
        return SmartIrrigationPlan.createDraft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                IrrigationPriority.HIGH,
                LocalDate.now(),
                now.plus(1, ChronoUnit.HOURS),
                now.plus(4, ChronoUnit.HOURS),
                BigDecimal.valueOf(150.0),
                180,
                BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(70.0),
                BigDecimal.valueOf(45.0),
                "Root-zone moisture deficit test",
                "Summary test",
                null,
                null
        );
    }

    @Test
    @DisplayName("Should successfully transition through standard operational lifecycle: DRAFT -> PROPOSED -> APPROVED -> SCHEDULED -> IN_PROGRESS -> COMPLETED")
    void testStandardLifecycleProgression() {
        SmartIrrigationPlan plan = createSamplePlan();
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.DRAFT);

        // DRAFT -> PROPOSED
        plan.propose();
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.PROPOSED);

        // PROPOSED -> APPROVED
        UUID userId = UUID.randomUUID();
        plan.approve(userId);
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.APPROVED);
        assertThat(plan.getApprovedBy()).isEqualTo(userId);
        assertThat(plan.getApprovedAt()).isNotNull();

        // APPROVED -> SCHEDULED
        plan.schedule();
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.SCHEDULED);

        // SCHEDULED -> IN_PROGRESS
        plan.start();
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.IN_PROGRESS);

        // IN_PROGRESS -> COMPLETED
        plan.complete(BigDecimal.valueOf(148.5));
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should permit direct approval from DRAFT")
    void testDirectApprovalFromDraft() {
        SmartIrrigationPlan plan = createSamplePlan();
        UUID userId = UUID.randomUUID();
        plan.approve(userId);
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.APPROVED);
    }

    @Test
    @DisplayName("Should reject approval when plan is blocked by hydraulic safety interlock")
    void testRejectApprovalWhenBlocked() {
        SmartIrrigationPlan plan = createSamplePlan();
        plan.block("Suspected pipe burst on main line");
        assertThat(plan.isBlocked()).isTrue();

        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> plan.approve(userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot approve plan blocked by safety interlock");
    }

    @Test
    @DisplayName("Should cancel plan with valid reason from any non-terminal state")
    void testCancellationRequiresReason() {
        SmartIrrigationPlan plan = createSamplePlan();

        // Blank reason rejected
        assertThatThrownBy(() -> plan.cancel("   "))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cancellation reason must not be blank");

        // Null reason rejected
        assertThatThrownBy(() -> plan.cancel(null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cancellation reason must not be blank");

        // Valid reason accepted
        plan.cancel("Weather shifted; heavy rainfall arrived");
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.CANCELLED);
        assertThat(plan.getCancellationReason()).isEqualTo("Weather shifted; heavy rainfall arrived");
    }

    @Test
    @DisplayName("Should prevent further mutations or transitions once completed or cancelled (terminal states)")
    void testTerminalStateImmutability() {
        SmartIrrigationPlan plan = createSamplePlan();
        plan.cancel("Operator cancelled turn");

        assertThatThrownBy(() -> plan.approve(UUID.randomUUID()))
                .isInstanceOf(BusinessRuleException.class);

        assertThatThrownBy(() -> plan.start())
                .isInstanceOf(BusinessRuleException.class);

        assertThatThrownBy(() -> plan.cancel("Another cancellation attempt"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Should expire active plan when execution window passes")
    void testPlanExpiration() {
        SmartIrrigationPlan plan = createSamplePlan();
        plan.propose();
        plan.expire();
        assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.EXPIRED);
    }
}
