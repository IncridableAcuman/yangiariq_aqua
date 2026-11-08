package com.aquaai.accountability;

import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.VerificationOutcome;
import com.aquaai.accountability.domain.service.IndependentVerificationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndependentVerificationEngineTest {

    private IndependentVerificationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new IndependentVerificationEngine();
    }

    @Test
    @DisplayName("Action owner cannot independently verify their own corrective action")
    void shouldRejectSelfVerificationWithoutOverride() {
        UUID userId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                engine.validateSeparationOfDuties(userId, userId, false));
    }

    @Test
    @DisplayName("Different verifier passes separation of duties check")
    void shouldAllowIndependentVerifier() {
        UUID actionOwnerId = UUID.randomUUID();
        UUID independentVerifierId = UUID.randomUUID();
        assertDoesNotThrow(() ->
                engine.validateSeparationOfDuties(actionOwnerId, independentVerifierId, false));
    }

    @Test
    @DisplayName("Administrative override allows documented self-verification exception")
    void shouldAllowSelfVerificationWithAdministrativeOverride() {
        UUID userId = UUID.randomUUID();
        assertDoesNotThrow(() ->
                engine.validateSeparationOfDuties(userId, userId, true));
    }

    @Test
    @DisplayName("Fully verified resolution maps to RESOLVED state")
    void shouldMapVerifiedResolvedToResolved() {
        assertEquals(IncidentStatus.RESOLVED,
                engine.determinePostVerificationStatus(VerificationOutcome.VERIFIED_RESOLVED));
    }

    @Test
    @DisplayName("Unresolved or partial verification outcome keeps incident in INVESTIGATING")
    void shouldMapIncompleteOutcomesToInvestigating() {
        assertEquals(IncidentStatus.INVESTIGATING,
                engine.determinePostVerificationStatus(VerificationOutcome.PARTIALLY_RESOLVED));
        assertEquals(IncidentStatus.INVESTIGATING,
                engine.determinePostVerificationStatus(VerificationOutcome.NOT_RESOLVED));
        assertEquals(IncidentStatus.INVESTIGATING,
                engine.determinePostVerificationStatus(VerificationOutcome.INSUFFICIENT_EVIDENCE));
    }
}
