package com.aquaai.risk.domain.model;

/**
 * Epistemological nature of supporting evidence.
 * Distinguishes verified real-world observations from predicted horizons and missing data.
 */
public enum RiskEvidenceType {
    OBSERVED,
    PREDICTED,
    MISSING,
    UNRELIABLE
}
