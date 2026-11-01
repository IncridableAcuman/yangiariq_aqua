package com.aquaai.irrigation.domain.model;

/**
 * Quality evaluation gate for incoming sensor and weather input data.
 */
public enum DataQualityGate {
    SUFFICIENT,
    PARTIAL,
    INSUFFICIENT,
    UNSAFE;

    public boolean canPlanWithConfidence() {
        return this == SUFFICIENT || this == PARTIAL;
    }

    public boolean isUnsafe() {
        return this == UNSAFE;
    }
}
