package com.aquaai.sensor.application.event;

import com.aquaai.sensor.domain.model.SensorReading;

import java.util.Objects;

/**
 * Application event published when a new sensor telemetry reading has been validated and recorded.
 */
public record TelemetryReadingRecordedEvent(SensorReading reading) {
    public TelemetryReadingRecordedEvent {
        Objects.requireNonNull(reading, "reading must not be null");
    }
}
