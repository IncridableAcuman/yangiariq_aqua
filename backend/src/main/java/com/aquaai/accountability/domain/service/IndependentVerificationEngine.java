package com.aquaai.accountability.domain.service;

import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.VerificationOutcome;

import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain service validating independent verification and enforcing separation of duties.
 */
public class IndependentVerificationEngine {

    public void validateSeparationOfDuties(UUID actionOwnerId, UUID verifierId, boolean administrativeOverride) {
        Objects.requireNonNull(verifierId, "Verifier ID cannot be null");
        if (actionOwnerId != null && actionOwnerId.equals(verifierId) && !administrativeOverride) {
            throw new IllegalArgumentException(
                    "Separation of duties violation: Action owner cannot independently verify their own corrective action without documented administrative override"
            );
        }
    }

    public IncidentStatus determinePostVerificationStatus(VerificationOutcome outcome) {
        Objects.requireNonNull(outcome, "Verification outcome cannot be null");
        return switch (outcome) {
            case VERIFIED_RESOLVED -> IncidentStatus.RESOLVED;
            case PARTIALLY_RESOLVED, NOT_RESOLVED, INSUFFICIENT_EVIDENCE -> IncidentStatus.INVESTIGATING;
        };
    }
}
