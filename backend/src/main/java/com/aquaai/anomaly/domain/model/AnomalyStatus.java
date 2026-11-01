package com.aquaai.anomaly.domain.model;

/**
 * Lifecycle states of an anomaly alert.
 */
public enum AnomalyStatus {
    DETECTED,
    ACKNOWLEDGED,
    INVESTIGATING,
    RESOLVED,
    DISMISSED;

    public boolean isTerminal() {
        return this == RESOLVED || this == DISMISSED;
    }

    public boolean isActive() {
        return this == DETECTED || this == ACKNOWLEDGED || this == INVESTIGATING;
    }
}
