package com.aquaai.risk.domain.model;

/**
 * Lifecycle states of an agricultural risk assessment.
 */
public enum RiskAssessmentLifecycleStatus {
    OPEN,
    ACKNOWLEDGED,
    ACTION_REQUIRED,
    MITIGATED,
    RESOLVED,
    EXPIRED;

    public boolean isTerminal() {
        return this == RESOLVED || this == EXPIRED;
    }

    public boolean isActive() {
        return this == OPEN || this == ACKNOWLEDGED || this == ACTION_REQUIRED;
    }
}
