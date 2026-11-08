package com.aquaai.accountability.domain.model;

/**
 * Trustworthiness and confidence classification of operational evidence.
 * Retains strict physical integrity: simulated data must never be upgraded to verified.
 */
public enum EvidenceConfidence {
    VERIFIED,
    PARTIALLY_VERIFIED,
    UNVERIFIED,
    SIMULATED,
    INSUFFICIENT_DATA;

    public boolean isReliable() {
        return this == VERIFIED || this == PARTIALLY_VERIFIED;
    }
}
