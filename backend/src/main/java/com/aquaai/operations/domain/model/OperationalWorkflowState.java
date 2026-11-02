package com.aquaai.operations.domain.model;

/**
 * Operational workflow state derived across telemetry, anomalies, recommendations,
 * water budget, smart irrigation plans, and operational execution events.
 */
public enum OperationalWorkflowState {
    DETECTED,
    REVIEW_REQUIRED,
    RECOMMENDATION_READY,
    PLAN_REQUIRED,
    PLAN_PROPOSED,
    APPROVAL_REQUIRED,
    APPROVED,
    SCHEDULED,
    READY,
    IN_PROGRESS,
    COMPLETED,
    VERIFIED,
    REJECTED,
    CANCELLED,
    EXPIRED,
    BLOCKED;

    public boolean isTerminal() {
        return this == VERIFIED || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }

    public boolean isActionable() {
        return this != CANCELLED && this != EXPIRED;
    }
}
