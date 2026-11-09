package com.aquaai.pilot.domain;

import com.aquaai.pilot.domain.model.PilotProgram;
import com.aquaai.pilot.domain.model.PilotStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PilotProgramUnitTest {

    @Test
    @DisplayName("Should create pilot program in DRAFT status")
    void shouldCreatePilotInDraft() {
        UUID ownerId = UUID.randomUUID();
        PilotProgram program = PilotProgram.create(
                "PILOT-TEST-01",
                "Yangiariq Drip Pilot",
                "Controlled pilot evaluating drip irrigation and soil moisture telemetry",
                "Yangiariq District, Khorezm",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 10, 1),
                "Yangiariq Water Users Association",
                ownerId,
                "Validate 15% conveyance conservation and 85% forecast accuracy"
        );

        assertThat(program.getId()).isNotNull();
        assertThat(program.getCode()).isEqualTo("PILOT-TEST-01");
        assertThat(program.getStatus()).isEqualTo(PilotStatus.DRAFT);
        assertThat(program.getApprovedBy()).isNull();
    }

    @Test
    @DisplayName("Should reject invalid date range where end is before start")
    void shouldRejectInvalidDateRange() {
        UUID ownerId = UUID.randomUUID();
        assertThatThrownBy(() -> PilotProgram.create(
                "PILOT-INVALID-01",
                "Invalid Dates Pilot",
                "Description",
                "Yangiariq",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 4, 1),
                "WUA",
                ownerId,
                "Objectives"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("end date cannot be before start date");
    }

    @Test
    @DisplayName("Should transition from DRAFT to APPROVED and then READY/ACTIVE")
    void shouldFollowStandardLifecycleTransitions() {
        UUID ownerId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        PilotProgram program = PilotProgram.create(
                "PILOT-LIFECYCLE-01",
                "Lifecycle Pilot",
                "Desc",
                "Scope",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 9, 1),
                "Org",
                ownerId,
                "Obj"
        );

        program.approve(approverId, "Approved after district water council review");
        assertThat(program.getStatus()).isEqualTo(PilotStatus.APPROVED);
        assertThat(program.getApprovedBy()).isEqualTo(approverId);
        assertThat(program.getApprovedAt()).isNotNull();

        program.markReady();
        assertThat(program.getStatus()).isEqualTo(PilotStatus.READY);

        program.activate();
        assertThat(program.getStatus()).isEqualTo(PilotStatus.ACTIVE);

        program.pause();
        assertThat(program.getStatus()).isEqualTo(PilotStatus.PAUSED);

        program.complete();
        assertThat(program.getStatus()).isEqualTo(PilotStatus.COMPLETED);

        // Terminal status cannot be updated
        assertThatThrownBy(() -> program.update("New Name", "Desc", "Scope", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 9, 1), "Org", "Obj"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update pilot program in terminal status");
    }
}
