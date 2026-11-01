package com.aquaai.sensor.domain.model;

/**
 * Quality status of telemetry readings.
 * Supports basic data integrity metadata and future automated anomaly flags.
 */
public enum ReadingQuality {
    VALID,
    SUSPECT,
    INVALID
}
