package com.aquaai.field.domain.model;

/**
 * Lifecycle status of an agricultural field.
 * <p>
 * ACTIVE: Field is currently farmed and managed in irrigation planning.
 * INACTIVE: Field exists but is temporarily unmanaged or retired.
 */
public enum FieldStatus {
    ACTIVE,
    INACTIVE
}
