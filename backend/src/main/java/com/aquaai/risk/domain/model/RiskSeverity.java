package com.aquaai.risk.domain.model;

/**
 * Standardized risk severity levels.
 * CRITICAL requires immediate attention; UNKNOWN indicates missing or unquantifiable status.
 */
public enum RiskSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN;

    public int getRank() {
        return switch (this) {
            case CRITICAL -> 1;
            case HIGH -> 2;
            case MEDIUM -> 3;
            case LOW -> 4;
            case UNKNOWN -> 5;
        };
    }
}
