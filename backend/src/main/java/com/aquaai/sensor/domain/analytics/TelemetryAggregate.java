package com.aquaai.sensor.domain.analytics;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable time-series aggregation bucket for telemetry measurements.
 */
public record TelemetryAggregate(
        UUID fieldId,
        UUID sensorId,
        SensorType sensorType,
        AggregationInterval interval,
        Instant bucketStartTime,
        Instant bucketEndTime,
        BigDecimal average,
        BigDecimal minimum,
        BigDecimal maximum,
        BigDecimal sum,
        BigDecimal firstReadingValue,
        Instant firstReadingAt,
        BigDecimal lastReadingValue,
        Instant lastReadingAt,
        long readingCount,
        MeasurementUnit unit
) {

    public TelemetryAggregate {
        Objects.requireNonNull(fieldId, "fieldId must not be null");
        Objects.requireNonNull(sensorType, "sensorType must not be null");
        Objects.requireNonNull(interval, "interval must not be null");
        Objects.requireNonNull(bucketStartTime, "bucketStartTime must not be null");
        Objects.requireNonNull(bucketEndTime, "bucketEndTime must not be null");
        Objects.requireNonNull(unit, "unit must not be null");

        if (bucketStartTime.isAfter(bucketEndTime)) {
            throw new IllegalArgumentException("bucketStartTime cannot be after bucketEndTime");
        }
        if (readingCount < 0) {
            throw new IllegalArgumentException("readingCount cannot be negative");
        }

        // Apply scale formatting for numeric values
        if (average != null) {
            average = average.setScale(4, RoundingMode.HALF_UP);
        }
        if (minimum != null) {
            minimum = minimum.setScale(4, RoundingMode.HALF_UP);
        }
        if (maximum != null) {
            maximum = maximum.setScale(4, RoundingMode.HALF_UP);
        }
        if (sum != null) {
            sum = sum.setScale(4, RoundingMode.HALF_UP);
        }
        if (firstReadingValue != null) {
            firstReadingValue = firstReadingValue.setScale(4, RoundingMode.HALF_UP);
        }
        if (lastReadingValue != null) {
            lastReadingValue = lastReadingValue.setScale(4, RoundingMode.HALF_UP);
        }
    }
}
