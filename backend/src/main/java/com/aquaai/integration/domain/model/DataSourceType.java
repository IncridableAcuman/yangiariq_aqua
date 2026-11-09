package com.aquaai.integration.domain.model;

/**
 * Categories of telemetry, meteorological, and operational data sources.
 */
public enum DataSourceType {
    SENSOR_DEVICE,
    WEATHER_PROVIDER,
    OPERATOR_INPUT,
    FARMER_INPUT,
    CSV_IMPORT,
    EXTERNAL_API,
    MANUAL_MEASUREMENT,
    SIMULATED_PROVIDER
}
