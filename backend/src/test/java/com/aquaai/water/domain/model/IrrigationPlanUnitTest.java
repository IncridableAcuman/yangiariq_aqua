package com.aquaai.water.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IrrigationPlanUnitTest {

    private final UUID fieldId = UUID.randomUUID();
    private final UUID waterAllocationId = UUID.randomUUID();
    private final Instant now = Instant.now();
    private final Instant start = now.plus(1, ChronoUnit.DAYS);
    private final Instant end = start.plus(6, ChronoUnit.HOURS);

    private IrrigationPlan createValidPlan() {
        return IrrigationPlan.create(
                fieldId,
                waterAllocationId,
                start,
                end,
                new BigDecimal("450.0000"),
                IrrigationMethod.FURROW,
                "Early vegetative furrow watering"
        );
    }

    @Nested
    @DisplayName("Creation & Invariant Tests")
    class CreationTests {

        @Test
        @DisplayName("Valid plan initializes properties and defaults status to PLANNED")
        void shouldCreateValidPlan() {
            IrrigationPlan plan = createValidPlan();

            assertThat(plan.getId()).isNotNull();
            assertThat(plan.getFieldId()).isEqualTo(fieldId);
            assertThat(plan.getWaterAllocationId()).isEqualTo(waterAllocationId);
            assertThat(plan.getPlannedStartAt()).isEqualTo(start);
            assertThat(plan.getPlannedEndAt()).isEqualTo(end);
            assertThat(plan.getPlannedVolumeM3()).isEqualByComparingTo("450.0000");
            assertThat(plan.getMethod()).isEqualTo(IrrigationMethod.FURROW);
            assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.PLANNED);
            assertThat(plan.isPlanned()).isTrue();
            assertThat(plan.isInProgress()).isFalse();
            assertThat(plan.isCompleted()).isFalse();
            assertThat(plan.isCancelled()).isFalse();
            assertThat(plan.getNotes()).isEqualTo("Early vegetative furrow watering");
            assertThat(plan.getCreatedAt()).isNotNull();
            assertThat(plan.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Creation without water allocation ID is allowed (optional quota reference)")
        void shouldAllowCreationWithoutAllocationId() {
            IrrigationPlan plan = IrrigationPlan.create(
                    fieldId,
                    null,
                    start,
                    end,
                    new BigDecimal("300.0"),
                    IrrigationMethod.DRIP,
                    "Direct well drip irrigation"
            );

            assertThat(plan.getWaterAllocationId()).isNull();
            assertThat(plan.getMethod()).isEqualTo(IrrigationMethod.DRIP);
        }

        @Test
        @DisplayName("Null field ID throws exception")
        void shouldRejectNullFieldId() {
            assertThatThrownBy(() -> IrrigationPlan.create(
                    null,
                    waterAllocationId,
                    start,
                    end,
                    new BigDecimal("300.0"),
                    IrrigationMethod.CANAL,
                    null
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Field ID must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.00", "-1.0", "-100.50"})
        @DisplayName("Non-positive planned volume throws BusinessRuleException")
        void shouldRejectNonPositiveVolume(String volumeStr) {
            assertThatThrownBy(() -> IrrigationPlan.create(
                    fieldId,
                    waterAllocationId,
                    start,
                    end,
                    new BigDecimal(volumeStr),
                    IrrigationMethod.CANAL,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("must be strictly greater than zero m³");
        }

        @Test
        @DisplayName("Planned start after planned end throws BusinessRuleException")
        void shouldRejectStartAfterEnd() {
            Instant invalidStart = end.plus(1, ChronoUnit.HOURS);
            assertThatThrownBy(() -> IrrigationPlan.create(
                    fieldId,
                    waterAllocationId,
                    invalidStart,
                    end,
                    new BigDecimal("100.0"),
                    IrrigationMethod.CANAL,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Planned start timestamp must be strictly before planned end timestamp");
        }

        @Test
        @DisplayName("Planned start equal to planned end throws BusinessRuleException")
        void shouldRejectStartEqualToEnd() {
            assertThatThrownBy(() -> IrrigationPlan.create(
                    fieldId,
                    waterAllocationId,
                    start,
                    start,
                    new BigDecimal("100.0"),
                    IrrigationMethod.CANAL,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Planned start timestamp must be strictly before planned end timestamp");
        }

        @Test
        @DisplayName("Null irrigation method throws BusinessRuleException")
        void shouldRejectNullMethod() {
            assertThatThrownBy(() -> IrrigationPlan.create(
                    fieldId,
                    waterAllocationId,
                    start,
                    end,
                    new BigDecimal("100.0"),
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Irrigation method must not be null");
        }

        @Test
        @DisplayName("Notes exceeding 500 characters throws BusinessRuleException")
        void shouldRejectOversizedNotes() {
            String longNotes = "P".repeat(501);
            assertThatThrownBy(() -> IrrigationPlan.create(
                    fieldId,
                    waterAllocationId,
                    start,
                    end,
                    new BigDecimal("100.0"),
                    IrrigationMethod.CANAL,
                    longNotes
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot exceed 500 characters");
        }
    }

    @Nested
    @DisplayName("State Transitions & Modification Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("Standard lifecycle: PLANNED -> IN_PROGRESS -> COMPLETED")
        void shouldExecuteStandardLifecycle() {
            IrrigationPlan plan = createValidPlan();

            // Start
            plan.start();
            assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.IN_PROGRESS);
            assertThat(plan.isInProgress()).isTrue();
            assertThat(plan.isPlanned()).isFalse();

            // Complete
            plan.complete();
            assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.COMPLETED);
            assertThat(plan.isCompleted()).isTrue();
            assertThat(plan.isInProgress()).isFalse();
        }

        @Test
        @DisplayName("PLANNED can transition directly to CANCELLED")
        void shouldCancelFromPlanned() {
            IrrigationPlan plan = createValidPlan();

            plan.cancel("Heavy rain forecasted");

            assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.CANCELLED);
            assertThat(plan.isCancelled()).isTrue();
            assertThat(plan.getNotes()).contains("Cancelled: Heavy rain forecasted");
        }

        @Test
        @DisplayName("IN_PROGRESS can transition to CANCELLED")
        void shouldCancelFromInProgress() {
            IrrigationPlan plan = createValidPlan();
            plan.start();

            plan.cancel("Canal intake siphon pipe burst");

            assertThat(plan.getStatus()).isEqualTo(IrrigationPlanStatus.CANCELLED);
            assertThat(plan.isCancelled()).isTrue();
            assertThat(plan.getNotes()).contains("Canal intake siphon pipe burst");
        }

        @Test
        @DisplayName("COMPLETED plan cannot be started")
        void shouldRejectStartingCompletedPlan() {
            IrrigationPlan plan = createValidPlan();
            plan.start();
            plan.complete();

            assertThatThrownBy(plan::start)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("expected PLANNED");
        }

        @Test
        @DisplayName("COMPLETED plan cannot be cancelled")
        void shouldRejectCancellingCompletedPlan() {
            IrrigationPlan plan = createValidPlan();
            plan.start();
            plan.complete();

            assertThatThrownBy(() -> plan.cancel("Attempt cancel completed"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot cancel an already COMPLETED irrigation plan");
        }

        @Test
        @DisplayName("CANCELLED plan cannot transition to another state")
        void shouldRejectMutatingCancelledPlan() {
            IrrigationPlan plan = createValidPlan();
            plan.cancel("Cancelled");

            assertThatThrownBy(plan::start)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("expected PLANNED");

            assertThatThrownBy(plan::complete)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("expected IN_PROGRESS");

            assertThatThrownBy(() -> plan.cancel("Again"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Irrigation plan is already CANCELLED");
        }

        @Test
        @DisplayName("PLANNED plan details can be updated")
        void shouldUpdateDetailsWhenPlanned() {
            IrrigationPlan plan = createValidPlan();
            BigDecimal newVolume = new BigDecimal("520.0000");
            Instant newStart = start.plus(2, ChronoUnit.HOURS);
            Instant newEnd = end.plus(2, ChronoUnit.HOURS);

            plan.updateDetails(waterAllocationId, newStart, newEnd, newVolume, IrrigationMethod.DRIP, "Shifted time slot");

            assertThat(plan.getPlannedVolumeM3()).isEqualByComparingTo(newVolume);
            assertThat(plan.getMethod()).isEqualTo(IrrigationMethod.DRIP);
            assertThat(plan.getPlannedStartAt()).isEqualTo(newStart);
            assertThat(plan.getPlannedEndAt()).isEqualTo(newEnd);
        }

        @Test
        @DisplayName("IN_PROGRESS plan details cannot be updated")
        void shouldRejectUpdateWhenInProgress() {
            IrrigationPlan plan = createValidPlan();
            plan.start();

            assertThatThrownBy(() -> plan.updateDetails(waterAllocationId, start, end, new BigDecimal("600.0"), IrrigationMethod.FURROW, "Notes"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("only PLANNED plans can be modified");
        }
    }
}
