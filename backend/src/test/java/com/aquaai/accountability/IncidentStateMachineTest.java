package com.aquaai.accountability;

import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.service.IncidentStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentStateMachineTest {

    private IncidentStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new IncidentStateMachine();
    }

    @ParameterizedTest
    @CsvSource({
            "DETECTED, UNDER_REVIEW",
            "DETECTED, OPEN",
            "DETECTED, DISMISSED",
            "UNDER_REVIEW, OPEN",
            "UNDER_REVIEW, DISMISSED",
            "OPEN, INVESTIGATING",
            "OPEN, ACTION_PROPOSED",
            "OPEN, DISMISSED",
            "INVESTIGATING, ACTION_PROPOSED",
            "INVESTIGATING, ACTION_IN_PROGRESS",
            "INVESTIGATING, DISMISSED",
            "ACTION_PROPOSED, ACTION_APPROVED",
            "ACTION_APPROVED, ACTION_IN_PROGRESS",
            "ACTION_IN_PROGRESS, PENDING_VERIFICATION",
            "PENDING_VERIFICATION, RESOLVED",
            "PENDING_VERIFICATION, INVESTIGATING",
            "RESOLVED, REOPENED",
            "RESOLVED, CLOSED",
            "DISMISSED, REOPENED",
            "REOPENED, INVESTIGATING",
            "CLOSED, REOPENED"
    })
    @DisplayName("Legal state transitions must be allowed")
    void shouldAllowLegalTransitions(IncidentStatus from, IncidentStatus to) {
        assertTrue(stateMachine.canTransition(from, to));
        assertDoesNotThrow(() -> stateMachine.validateTransition(from, to, "Valid operational justification"));
    }

    @ParameterizedTest
    @CsvSource({
            "DETECTED, RESOLVED",
            "DETECTED, CLOSED",
            "UNDER_REVIEW, RESOLVED",
            "ACTION_PROPOSED, RESOLVED",
            "ACTION_APPROVED, RESOLVED",
            "ACTION_IN_PROGRESS, RESOLVED",
            "PENDING_VERIFICATION, CLOSED",
            "DISMISSED, CLOSED",
            "DISMISSED, ACTION_APPROVED"
    })
    @DisplayName("Illegal arbitrary state transitions must be rejected")
    void shouldRejectIllegalTransitions(IncidentStatus from, IncidentStatus to) {
        assertFalse(stateMachine.canTransition(from, to));
        assertThrows(IllegalStateException.class, () -> stateMachine.validateTransition(from, to, "Attempting skip"));
    }

    @Test
    @DisplayName("Transition validation must enforce a mandatory non-blank audit reason")
    void shouldRequireReasonForValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                stateMachine.validateTransition(IncidentStatus.DETECTED, IncidentStatus.UNDER_REVIEW, ""));
        assertThrows(IllegalArgumentException.class, () ->
                stateMachine.validateTransition(IncidentStatus.DETECTED, IncidentStatus.UNDER_REVIEW, "   "));
        assertThrows(IllegalArgumentException.class, () ->
                stateMachine.validateTransition(IncidentStatus.DETECTED, IncidentStatus.UNDER_REVIEW, null));
    }
}
