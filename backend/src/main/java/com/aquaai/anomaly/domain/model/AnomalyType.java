package com.aquaai.anomaly.domain.model;

/**
 * Standard classification of telemetry anomalies and agricultural risks.
 */
public enum AnomalyType {
    SOIL_MOISTURE_DROP,
    SOIL_MOISTURE_CRITICAL,
    WATER_FLOW_SPIKE,
    WATER_FLOW_ZERO,
    WATER_PRESSURE_DROP,
    WATER_LEVEL_DROP,
    SALINITY_INCREASE,
    TEMPERATURE_ANOMALY,
    HUMIDITY_ANOMALY,
    RAINFALL_ANOMALY,
    SENSOR_OFFLINE,
    POSSIBLE_SENSOR_DRIFT,
    SENSOR_STUCK,
    SENSOR_OUTLIER,
    IRRIGATION_FAILURE,
    SUSPECTED_PIPE_BURST
}
