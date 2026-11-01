package com.aquaai.intelligence.domain.port;

import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain port for querying physical sensor telemetry.
 */
public interface IntelligenceTelemetryPort {

    List<SensorReading> getLatestReadings(UUID fieldId);

    List<SensorReading> getRecentReadings(UUID fieldId, SensorType type, Instant from, Instant to);
}
