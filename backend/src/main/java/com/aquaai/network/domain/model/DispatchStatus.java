package com.aquaai.network.domain.model;

/**
 * Human-governed lifecycle states for regional canal dispatch schedules.
 */
public enum DispatchStatus {
    DRAFT,
    OPTIMIZING,
    PROPOSED,
    REVIEW_REQUIRED,
    APPROVED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    CANCELLED,
    EXPIRED,
    BLOCKED
}
