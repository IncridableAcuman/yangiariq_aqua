package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TelemetryAggregateResponse(
        UUID fieldId,
        UUID sensorId,
        SensorType sensorType,
        String interval,
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

    public static TelemetryAggregateResponse from(TelemetryAggregate agg) {
        if (agg == null) {
            return null;
        }
        return new TelemetryAggregateResponse(
                agg.fieldId(),
                agg.sensorId(),
                agg.sensorType(),
                agg.interval().getCode(),
                agg.bucketStartTime(),
                agg.bucketEndTime(),
                agg.average(),
                agg.minimum(),
                agg.maximum(),
                agg.sum(),
                agg.firstReadingValue(),
                agg.firstReadingAt(),
                agg.lastReadingValue(),
                agg.lastReadingAt(),
                agg.readingCount(),
                agg.unit()
        );
    }
}
