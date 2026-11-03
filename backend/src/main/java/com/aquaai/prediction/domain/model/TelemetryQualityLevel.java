package com.aquaai.prediction.domain.model;

/**
 * Normalized telemetry transmission quality level for parcel prediction context.
 */
public enum TelemetryQualityLevel {
    HIGH,          // >= 85% valid readings, fresh within SLA
    MEDIUM,        // 60-84% valid readings
    LOW,           // 30-59% valid readings or intermittent packet loss
    INSUFFICIENT,  // < 30% valid readings or stale telemetry
    UNKNOWN
}
