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

class WaterAllocationUnitTest {

    private final UUID fieldId = UUID.randomUUID();
    private final UUID waterSourceId = UUID.randomUUID();
    private final Instant now = Instant.now();
    private final Instant start = now.minus(5, ChronoUnit.DAYS);
    private final Instant end = now.plus(25, ChronoUnit.DAYS);

    private WaterAllocation createValidAllocation() {
        return WaterAllocation.create(
                fieldId,
                waterSourceId,
                new BigDecimal("1500.5000"),
                start,
                end,
                "Spring 2026 Cotton Quota"
        );
    }

    @Nested
    @DisplayName("Creation & Invariant Tests")
    class CreationTests {

        @Test
        @DisplayName("Valid allocation sets all attributes and defaults status to ACTIVE")
        void shouldCreateValidAllocation() {
            WaterAllocation allocation = createValidAllocation();

            assertThat(allocation.getId()).isNotNull();
            assertThat(allocation.getFieldId()).isEqualTo(fieldId);
            assertThat(allocation.getWaterSourceId()).isEqualTo(waterSourceId);
            assertThat(allocation.getAllocatedVolumeM3()).isEqualByComparingTo("1500.5000");
            assertThat(allocation.getPeriodStart()).isEqualTo(start);
            assertThat(allocation.getPeriodEnd()).isEqualTo(end);
            assertThat(allocation.getStatus()).isEqualTo(WaterAllocationStatus.ACTIVE);
            assertThat(allocation.getNotes()).isEqualTo("Spring 2026 Cotton Quota");
            assertThat(allocation.isActive()).isTrue();
            assertThat(allocation.isClosed()).isFalse();
            assertThat(allocation.isCancelled()).isFalse();
            assertThat(allocation.getCreatedAt()).isNotNull();
            assertThat(allocation.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Null field ID throws exception")
        void shouldRejectNullFieldId() {
            assertThatThrownBy(() -> WaterAllocation.create(
                    null,
                    waterSourceId,
                    new BigDecimal("100.0"),
                    start,
                    end,
                    null
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Field ID must not be null");
        }

        @Test
        @DisplayName("Null water source ID throws exception")
        void shouldRejectNullWaterSourceId() {
            assertThatThrownBy(() -> WaterAllocation.create(
                    fieldId,
                    null,
                    new BigDecimal("100.0"),
                    start,
                    end,
                    null
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("WaterSource ID must not be null");
        }

        @Test
        @DisplayName("Null volume throws BusinessRuleException")
        void shouldRejectNullVolume() {
            assertThatThrownBy(() -> WaterAllocation.create(
                    fieldId,
                    waterSourceId,
                    null,
                    start,
                    end,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Allocated volume must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.0", "-0.001", "-500.00"})
        @DisplayName("Non-positive allocated volume throws BusinessRuleException")
        void shouldRejectNonPositiveVolume(String volumeStr) {
            assertThatThrownBy(() -> WaterAllocation.create(
                    fieldId,
                    waterSourceId,
                    new BigDecimal(volumeStr),
                    start,
                    end,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("must be strictly greater than zero m³");
        }

        @Test
        @DisplayName("Period start after period end throws BusinessRuleException")
        void shouldRejectStartAfterEnd() {
            Instant invalidStart = end.plus(1, ChronoUnit.DAYS);
            assertThatThrownBy(() -> WaterAllocation.create(
                    fieldId,
                    waterSourceId,
                    new BigDecimal("200.0"),
                    invalidStart,
                    end,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("start timestamp cannot be after period end timestamp");
        }

        @Test
        @DisplayName("Equal period start and end is accepted (single-day allocation)")
        void shouldAllowEqualStartAndEnd() {
            WaterAllocation allocation = WaterAllocation.create(
                    fieldId,
                    waterSourceId,
                    new BigDecimal("50.0"),
                    start,
                    start,
                    "Single event window"
            );
            assertThat(allocation.getPeriodStart()).isEqualTo(allocation.getPeriodEnd());
        }

        @Test
        @DisplayName("Oversized notes exceeding 500 characters throws BusinessRuleException")
        void shouldRejectOversizedNotes() {
            String longNotes = "N".repeat(501);
            assertThatThrownBy(() -> WaterAllocation.create(
                    fieldId,
                    waterSourceId,
                    new BigDecimal("100.0"),
                    start,
                    end,
                    longNotes
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot exceed 500 characters");
        }
    }

    @Nested
    @DisplayName("State Transitions & Modification Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("ACTIVE allocation can be updated")
        void shouldUpdateActiveAllocation() {
            WaterAllocation allocation = createValidAllocation();
            BigDecimal newVolume = new BigDecimal("1800.0000");
            Instant newEnd = end.plus(10, ChronoUnit.DAYS);

            allocation.updateAllocation(newVolume, start, newEnd, "Increased quota approved by operator");

            assertThat(allocation.getAllocatedVolumeM3()).isEqualByComparingTo(newVolume);
            assertThat(allocation.getPeriodEnd()).isEqualTo(newEnd);
            assertThat(allocation.getNotes()).isEqualTo("Increased quota approved by operator");
        }

        @Test
        @DisplayName("ACTIVE allocation can transition to CLOSED")
        void shouldCloseActiveAllocation() {
            WaterAllocation allocation = createValidAllocation();

            allocation.close("Season concluded, gates locked");

            assertThat(allocation.getStatus()).isEqualTo(WaterAllocationStatus.CLOSED);
            assertThat(allocation.isClosed()).isTrue();
            assertThat(allocation.isActive()).isFalse();
            assertThat(allocation.getNotes()).contains("Season concluded, gates locked");
        }

        @Test
        @DisplayName("ACTIVE allocation can transition to CANCELLED")
        void shouldCancelActiveAllocation() {
            WaterAllocation allocation = createValidAllocation();

            allocation.cancel("Canal break maintenance in sector 4");

            assertThat(allocation.getStatus()).isEqualTo(WaterAllocationStatus.CANCELLED);
            assertThat(allocation.isCancelled()).isTrue();
            assertThat(allocation.isActive()).isFalse();
            assertThat(allocation.getNotes()).contains("Canal break maintenance in sector 4");
        }

        @Test
        @DisplayName("CLOSED allocation cannot transition to CANCELLED")
        void shouldRejectTransitionFromClosedToCancelled() {
            WaterAllocation allocation = createValidAllocation();
            allocation.close(null);

            assertThatThrownBy(() -> allocation.cancel("Attempt cancel"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("only ACTIVE allocations can be cancelled");
        }

        @Test
        @DisplayName("CANCELLED allocation cannot transition to CLOSED")
        void shouldRejectTransitionFromCancelledToClosed() {
            WaterAllocation allocation = createValidAllocation();
            allocation.cancel(null);

            assertThatThrownBy(() -> allocation.close("Attempt close"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("only ACTIVE allocations can be closed");
        }

        @Test
        @DisplayName("CLOSED allocation cannot be updated")
        void shouldRejectUpdateOnClosedAllocation() {
            WaterAllocation allocation = createValidAllocation();
            allocation.close(null);

            assertThatThrownBy(() -> allocation.updateAllocation(new BigDecimal("999.0"), start, end, "Notes"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("only ACTIVE allocations can be updated");
        }

        @Test
        @DisplayName("CANCELLED allocation cannot be updated")
        void shouldRejectUpdateOnCancelledAllocation() {
            WaterAllocation allocation = createValidAllocation();
            allocation.cancel(null);

            assertThatThrownBy(() -> allocation.updateAllocation(new BigDecimal("999.0"), start, end, "Notes"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("only ACTIVE allocations can be updated");
        }
    }

    @Nested
    @DisplayName("Period Active Check Tests")
    class PeriodCheckTests {

        @Test
        @DisplayName("isPeriodActiveAt returns true within period for ACTIVE allocation")
        void shouldBeActiveWithinPeriod() {
            WaterAllocation allocation = createValidAllocation();
            Instant midPeriod = start.plus(5, ChronoUnit.DAYS);

            assertThat(allocation.isPeriodActiveAt(midPeriod)).isTrue();
            assertThat(allocation.isPeriodActiveAt(start)).isTrue();
            assertThat(allocation.isPeriodActiveAt(end)).isTrue();
        }

        @Test
        @DisplayName("isPeriodActiveAt returns false outside period or when CLOSED")
        void shouldBeFalseOutsidePeriodOrClosed() {
            WaterAllocation allocation = createValidAllocation();
            Instant before = start.minus(1, ChronoUnit.DAYS);
            Instant after = end.plus(1, ChronoUnit.DAYS);

            assertThat(allocation.isPeriodActiveAt(before)).isFalse();
            assertThat(allocation.isPeriodActiveAt(after)).isFalse();

            allocation.close(null);
            assertThat(allocation.isPeriodActiveAt(now)).isFalse();
        }
    }
}
