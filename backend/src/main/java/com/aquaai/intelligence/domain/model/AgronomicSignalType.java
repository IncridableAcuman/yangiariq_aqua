package com.aquaai.intelligence.domain.model;

/**
 * Standardized agronomic signals identified by the telemetry intelligence layer.
 */
public enum AgronomicSignalType {
    SOIL_TOO_DRY,
    SOIL_TOO_WET,
    HIGH_TEMPERATURE,
    LOW_TEMPERATURE,
    HIGH_HUMIDITY,
    LOW_HUMIDITY,
    LOW_WATER_LEVEL,
    ABNORMAL_FLOW,
    PRESSURE_DROP,
    NO_RECENT_TELEMETRY
}
