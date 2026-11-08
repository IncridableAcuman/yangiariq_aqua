package com.aquaai.accountability.domain.model;

/**
 * Outcome resulting from independent verification of a corrective action.
 */
public enum VerificationOutcome {
    VERIFIED_RESOLVED,
    PARTIALLY_RESOLVED,
    NOT_RESOLVED,
    INSUFFICIENT_EVIDENCE;

    public boolean isFullyResolved() {
        return this == VERIFIED_RESOLVED;
    }
}
