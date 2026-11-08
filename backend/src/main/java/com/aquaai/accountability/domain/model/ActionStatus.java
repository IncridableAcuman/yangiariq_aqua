package com.aquaai.accountability.domain.model;

/**
 * Execution lifecycle status for a proposed corrective action.
 */
public enum ActionStatus {
    PROPOSED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }
}
