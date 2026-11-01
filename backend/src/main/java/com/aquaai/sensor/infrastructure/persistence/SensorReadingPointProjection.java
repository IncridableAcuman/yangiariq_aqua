package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * High-performance Spring Data projection for time-series analytics and bucket aggregation.
 */
public interface SensorReadingPointProjection {
    UUID getId();
    UUID getSensorId();
    UUID getFieldId();
    SensorType getType();
    BigDecimal getValue();
    MeasurementUnit getUnit();
    ReadingQuality getQuality();
    String getSource();
    Instant getMeasuredAt();
}
