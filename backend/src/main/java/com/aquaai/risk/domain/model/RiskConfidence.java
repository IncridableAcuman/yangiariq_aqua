package com.aquaai.risk.domain.model;

/**
 * Standardized risk confidence tiers reflecting evidence quality and sensor reliability.
 * Confidence and Severity are strictly independent dimensions.
 */
public enum RiskConfidence {
    HIGH,
    MEDIUM,
    LOW,
    INSUFFICIENT_DATA;

    public boolean isAuthoritative() {
        return this == HIGH;
    }
}
