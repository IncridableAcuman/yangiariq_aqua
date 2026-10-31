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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IrrigationEventUnitTest {

    private final UUID fieldId = UUID.randomUUID();
    private final UUID planId = UUID.randomUUID();
    private final Instant now = Instant.now();
    private final Instant startedAt = now.minus(3, ChronoUnit.HOURS);
    private final Instant endedAt = now.minus(30, ChronoUnit.MINUTES);

    @Nested
    @DisplayName("Creation & Invariant Tests")
    class CreationTests {

        @Test
        @DisplayName("Valid start() initializes event in STARTED status without completion fields")
        void shouldCreateStartedEvent() {
            IrrigationEvent event = IrrigationEvent.start(fieldId, planId, startedAt, "Starting evening furrow watering");

            assertThat(event.getId()).isNotNull();
            assertThat(event.getFieldId()).isEqualTo(fieldId);
            assertThat(event.getIrrigationPlanId()).isEqualTo(planId);
            assertThat(event.getStartedAt()).isEqualTo(startedAt);
            assertThat(event.getEndedAt()).isNull();
            assertThat(event.getActualVolumeM3()).isNull();
            assertThat(event.getStatus()).isEqualTo(IrrigationEventStatus.STARTED);
            assertThat(event.isStarted()).isTrue();
            assertThat(event.isCompleted()).isFalse();
            assertThat(event.isCancelled()).isFalse();
            assertThat(event.getNotes()).isEqualTo("Starting evening furrow watering");
            assertThat(event.getCreatedAt()).isNotNull();
            assertThat(event.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("start() with null startedAt defaults to current timestamp")
        void shouldDefaultStartedAtWhenNull() {
            Instant before = Instant.now();
            IrrigationEvent event = IrrigationEvent.start(fieldId, null, null, null);
            Instant after = Instant.now();

            assertThat(event.getStartedAt()).isNotNull();
            assertThat(event.getStartedAt()).isBetween(before, after);
            assertThat(event.getIrrigationPlanId()).isNull();
        }

        @Test
        @DisplayName("Creation with explicit ID preserves provided UUID")
        void shouldCreateWithExplicitId() {
            UUID customId = UUID.randomUUID();
            IrrigationEvent event = IrrigationEvent.start(customId, fieldId, planId, startedAt, null);

            assertThat(event.getId()).isEqualTo(customId);
        }

        @Test
        @DisplayName("recordCompleted() directly initializes completed event with verified volume")
        void shouldCreateRecordCompletedEvent() {
            IrrigationEvent event = IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    endedAt,
                    new BigDecimal("380.5000"),
                    "Finished furrow watering early due to rain"
            );

            assertThat(event.getStatus()).isEqualTo(IrrigationEventStatus.COMPLETED);
            assertThat(event.isCompleted()).isTrue();
            assertThat(event.getEndedAt()).isEqualTo(endedAt);
            assertThat(event.getActualVolumeM3()).isEqualByComparingTo("380.5000");
        }

        @Test
        @DisplayName("Null field ID throws NullPointerException")
        void shouldRejectNullFieldId() {
            assertThatThrownBy(() -> IrrigationEvent.start(null, planId, startedAt, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Field ID must not be null");
        }

        @Test
        @DisplayName("Reconstitution with null startedAt throws NullPointerException")
        void shouldRejectNullStartedAt() {
            assertThatThrownBy(() -> new IrrigationEvent(
                    UUID.randomUUID(),
                    fieldId,
                    planId,
                    null,
                    null,
                    null,
                    IrrigationEventStatus.STARTED,
                    null,
                    now,
                    now,
                    null
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Started at timestamp must not be null");
        }

        @Test
        @DisplayName("Actual volume zero is valid (e.g. pump failure after starting)")
        void shouldAllowZeroActualVolume() {
            IrrigationEvent event = IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    endedAt,
                    BigDecimal.ZERO,
                    "Pump failure, zero delivery"
            );

            assertThat(event.getActualVolumeM3()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @ParameterizedTest
        @ValueSource(strings = {"-0.0001", "-1.0", "-500.25"})
        @DisplayName("Negative actual volume throws BusinessRuleException")
        void shouldRejectNegativeVolume(String volumeStr) {
            BigDecimal negativeVol = new BigDecimal(volumeStr);
            assertThatThrownBy(() -> IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    endedAt,
                    negativeVol,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Actual volume must be greater than or equal to zero m³");
        }

        @Test
        @DisplayName("Ended at timestamp before started at throws BusinessRuleException")
        void shouldRejectEndedAtBeforeStartedAt() {
            Instant invalidEndedAt = startedAt.minus(10, ChronoUnit.MINUTES);
            assertThatThrownBy(() -> IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    invalidEndedAt,
                    new BigDecimal("100.0"),
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Ended at timestamp must be strictly after started at timestamp");
        }

        @Test
        @DisplayName("Ended at timestamp equal to started at throws BusinessRuleException")
        void shouldRejectEndedAtEqualToStartedAt() {
            assertThatThrownBy(() -> IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    startedAt,
                    new BigDecimal("100.0"),
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Ended at timestamp must be strictly after started at timestamp");
        }

        @Test
        @DisplayName("recordCompleted() with null endedAt throws BusinessRuleException")
        void shouldRejectNullEndedAtForCompleted() {
            assertThatThrownBy(() -> IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    null,
                    new BigDecimal("100.0"),
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Ended at timestamp must not be null for completed irrigation");
        }

        @Test
        @DisplayName("recordCompleted() with null actual volume throws BusinessRuleException")
        void shouldRejectNullVolumeForCompleted() {
            assertThatThrownBy(() -> IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    endedAt,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Actual volume must not be null for completed irrigation");
        }
    }

    @Nested
    @DisplayName("Status Transitions & Lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("complete() transitions STARTED to COMPLETED and sets volume and time")
        void shouldCompleteStartedEvent() {
            IrrigationEvent event = IrrigationEvent.start(fieldId, planId, startedAt, "Running");

            event.complete(endedAt, new BigDecimal("420.0000"), "Finished normally");

            assertThat(event.getStatus()).isEqualTo(IrrigationEventStatus.COMPLETED);
            assertThat(event.isCompleted()).isTrue();
            assertThat(event.isStarted()).isFalse();
            assertThat(event.getEndedAt()).isEqualTo(endedAt);
            assertThat(event.getActualVolumeM3()).isEqualByComparingTo("420.0000");
            assertThat(event.getNotes()).isEqualTo("Running | Finished normally");
        }

        @Test
        @DisplayName("complete() on already COMPLETED event throws BusinessRuleException")
        void shouldRejectCompletingAlreadyCompletedEvent() {
            IrrigationEvent event = IrrigationEvent.recordCompleted(fieldId, planId, startedAt, endedAt, new BigDecimal("100.0"), null);

            assertThatThrownBy(() -> event.complete(endedAt.plus(1, ChronoUnit.HOURS), new BigDecimal("150.0"), "Again"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot complete irrigation event in status COMPLETED");
        }

        @Test
        @DisplayName("cancel() transitions STARTED to CANCELLED")
        void shouldCancelStartedEvent() {
            IrrigationEvent event = IrrigationEvent.start(fieldId, planId, startedAt, "Starting");

            event.cancel("Canal intake blocked");

            assertThat(event.getStatus()).isEqualTo(IrrigationEventStatus.CANCELLED);
            assertThat(event.isCancelled()).isTrue();
            assertThat(event.isStarted()).isFalse();
            assertThat(event.getNotes()).isEqualTo("Starting | Cancelled: Canal intake blocked");
        }

        @Test
        @DisplayName("cancel() on COMPLETED event throws BusinessRuleException")
        void shouldRejectCancellingCompletedEvent() {
            IrrigationEvent event = IrrigationEvent.recordCompleted(fieldId, planId, startedAt, endedAt, new BigDecimal("100.0"), null);

            assertThatThrownBy(() -> event.cancel("Too late"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot cancel an already COMPLETED irrigation event");
        }

        @Test
        @DisplayName("cancel() on already CANCELLED event throws BusinessRuleException")
        void shouldRejectCancellingCancelledEvent() {
            IrrigationEvent event = IrrigationEvent.start(fieldId, planId, startedAt, null);
            event.cancel("First cancellation");

            assertThatThrownBy(() -> event.cancel("Second cancellation"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Irrigation event is already CANCELLED");
        }

        @Test
        @DisplayName("complete() on CANCELLED event throws BusinessRuleException")
        void shouldRejectCompletingCancelledEvent() {
            IrrigationEvent event = IrrigationEvent.start(fieldId, planId, startedAt, null);
            event.cancel("Cancelled early");

            assertThatThrownBy(() -> event.complete(endedAt, new BigDecimal("50.0"), "Trying to complete"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot complete irrigation event in status CANCELLED");
        }
    }

    @Nested
    @DisplayName("Water Variance & Consumption Calculations")
    class VarianceTests {

        @Test
        @DisplayName("Water conservation: Planned 500 m³, Actual 470 m³ yields +30 m³ saved")
        void shouldCalculateWaterConservation() {
            IrrigationEvent event = IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    endedAt,
                    new BigDecimal("470.0000"),
                    null
            );

            Optional<BigDecimal> variance = event.calculateVarianceFromPlanned(new BigDecimal("500.0000"));

            assertThat(variance).isPresent();
            assertThat(variance.get()).isEqualByComparingTo("30.0000");
        }

        @Test
        @DisplayName("Water excess: Planned 500 m³, Actual 530 m³ yields -30 m³ excess")
        void shouldCalculateWaterExcess() {
            IrrigationEvent event = IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    endedAt,
                    new BigDecimal("530.0000"),
                    null
            );

            Optional<BigDecimal> variance = event.calculateVarianceFromPlanned(new BigDecimal("500.0000"));

            assertThat(variance).isPresent();
            assertThat(variance.get()).isEqualByComparingTo("-30.0000");
        }

        @Test
        @DisplayName("Variance returns empty if actual volume is not yet recorded")
        void shouldReturnEmptyVarianceWhenActualVolumeNull() {
            IrrigationEvent event = IrrigationEvent.start(fieldId, planId, startedAt, null);

            Optional<BigDecimal> variance = event.calculateVarianceFromPlanned(new BigDecimal("500.0000"));

            assertThat(variance).isEmpty();
        }

        @Test
        @DisplayName("Variance returns empty if planned volume argument is null")
        void shouldReturnEmptyVarianceWhenPlannedVolumeNull() {
            IrrigationEvent event = IrrigationEvent.recordCompleted(
                    fieldId,
                    planId,
                    startedAt,
                    endedAt,
                    new BigDecimal("400.0"),
                    null
            );

            Optional<BigDecimal> variance = event.calculateVarianceFromPlanned(null);

            assertThat(variance).isEmpty();
        }
    }

    @Nested
    @DisplayName("Notes Validation")
    class NotesTests {

        @Test
        @DisplayName("Notes exceeding 500 characters throws BusinessRuleException")
        void shouldRejectOversizedNotes() {
            String oversized = "N".repeat(501);
            assertThatThrownBy(() -> IrrigationEvent.start(fieldId, planId, startedAt, oversized))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Irrigation event notes cannot exceed 500 characters");
        }

        @Test
        @DisplayName("Blank notes are normalized to null")
        void shouldNormalizeBlankNotesToNull() {
            IrrigationEvent event = IrrigationEvent.start(fieldId, planId, startedAt, "   ");
            assertThat(event.getNotes()).isNull();
        }
    }
}
