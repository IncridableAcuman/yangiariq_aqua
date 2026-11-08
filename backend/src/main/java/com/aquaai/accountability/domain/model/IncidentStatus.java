package com.aquaai.accountability.domain.model;

/**
 * Formal operational lifecycle status of an incident investigation case.
 */
public enum IncidentStatus {
    DETECTED,
    UNDER_REVIEW,
    OPEN,
    INVESTIGATING,
    ACTION_PROPOSED,
    ACTION_APPROVED,
    ACTION_IN_PROGRESS,
    PENDING_VERIFICATION,
    RESOLVED,
    DISMISSED,
    REOPENED,
    CLOSED;

    public boolean isTerminal() {
        return this == RESOLVED || this == DISMISSED || this == CLOSED;
    }

    public boolean isActive() {
        return !isTerminal();
    }

    public boolean allowsInvestigation() {
        return this == OPEN || this == INVESTIGATING || this == ACTION_PROPOSED ||
                this == ACTION_APPROVED || this == ACTION_IN_PROGRESS || this == REOPENED;
    }
}
