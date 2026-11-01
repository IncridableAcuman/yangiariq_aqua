package com.aquaai.irrigation.domain.model;

/**
 * State machine lifecycle enumeration for smart irrigation plans.
 * <p>
 * Progression:
 * DRAFT -> PROPOSED -> APPROVED -> SCHEDULED -> IN_PROGRESS -> COMPLETED
 * Alternate terminal states: CANCELLED (requires reason), EXPIRED (missed execution window).
 */
public enum IrrigationPlanStatus {
    DRAFT,
    PROPOSED,
    APPROVED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    EXPIRED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == EXPIRED;
    }

    public boolean canBeCancelled() {
        return this != COMPLETED && this != CANCELLED && this != EXPIRED;
    }

    public boolean canBeApproved() {
        return this == PROPOSED || this == DRAFT;
    }

    public boolean isActive() {
        return this == PROPOSED || this == APPROVED || this == SCHEDULED || this == IN_PROGRESS;
    }
}
