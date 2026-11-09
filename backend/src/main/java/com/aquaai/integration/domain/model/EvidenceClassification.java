package com.aquaai.integration.domain.model;

/**
 * Rigorous evidence classification preventing simulated data from ever being
 * represented as verified physical observations.
 */
public enum EvidenceClassification {
    OBSERVED,
    VERIFIED,
    ESTIMATED,
    CALCULATED,
    SIMULATED,
    MISSING;

    public boolean isRealObservation() {
        return this == OBSERVED || this == VERIFIED;
    }
}
