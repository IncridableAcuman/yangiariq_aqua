package com.aquaai.irrigation.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable value object representing a specific constraint evaluated during irrigation planning.
 */
@Getter
@Builder
public class PlanConstraint {

    private final UUID id;
    private final ConstraintType type;
    private final ConstraintStatus status;
    private final String message;
    private final String details;

    public PlanConstraint(UUID id, ConstraintType type, ConstraintStatus status, String message, String details) {
        this.id = id != null ? id : UUID.randomUUID();
        this.type = Objects.requireNonNull(type, "ConstraintType must not be null");
        this.status = Objects.requireNonNull(status, "ConstraintStatus must not be null");
        this.message = Objects.requireNonNull(message, "Constraint message must not be null");
        this.details = details;
    }

    public boolean isBlocking() {
        return this.status == ConstraintStatus.BLOCKED || this.status == ConstraintStatus.VIOLATED;
    }

    public boolean isWarning() {
        return this.status == ConstraintStatus.WARNING;
    }
}
