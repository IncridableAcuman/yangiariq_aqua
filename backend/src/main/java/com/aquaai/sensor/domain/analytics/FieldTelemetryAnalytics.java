package com.aquaai.sensor.domain.analytics;

import com.aquaai.sensor.domain.model.SensorType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregated multi-modal historical telemetry analysis for a designated agricultural parcel.
 */
public record FieldTelemetryAnalytics(
        UUID fieldId,
        Instant periodStart,
        Instant periodEnd,
        AggregationInterval interval,
        Map<SensorType, List<TelemetryAggregate>> aggregatesByType,
        SensorAvailabilitySummary availability,
        DataQualityMetrics dataQuality,
        Instant generatedAt
) {

    public FieldTelemetryAnalytics {
        Objects.requireNonNull(fieldId, "fieldId must not be null");
        Objects.requireNonNull(periodStart, "periodStart must not be null");
        Objects.requireNonNull(periodEnd, "periodEnd must not be null");
        Objects.requireNonNull(interval, "interval must not be null");
        Objects.requireNonNull(generatedAt, "generatedAt must not be null");

        aggregatesByType = aggregatesByType != null ? Map.copyOf(aggregatesByType) : Collections.emptyMap();
        availability = availability != null ? availability : SensorAvailabilitySummary.empty();
        dataQuality = dataQuality != null ? dataQuality : DataQualityMetrics.calculate(0, 0, 0, 0, 0, 0);
    }
}
