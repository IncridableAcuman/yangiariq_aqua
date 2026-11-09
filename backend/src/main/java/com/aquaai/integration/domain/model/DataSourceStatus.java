package com.aquaai.integration.domain.model;

/**
 * Operational and connectivity lifecycle status of an external or simulated data source.
 */
public enum DataSourceStatus {
    ACTIVE,
    INACTIVE,
    DEGRADED,
    NOT_CONFIGURED,
    DISCONNECTED,
    ERROR;

    public boolean isOperational() {
        return this == ACTIVE || this == DEGRADED;
    }
}
