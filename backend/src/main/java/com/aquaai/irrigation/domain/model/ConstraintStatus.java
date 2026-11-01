package com.aquaai.irrigation.domain.model;

/**
 * Status outcome of an evaluated irrigation planning constraint.
 */
public enum ConstraintStatus {
    SATISFIED,
    WARNING,
    VIOLATED,
    BLOCKED
}
