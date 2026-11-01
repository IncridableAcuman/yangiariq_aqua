package com.aquaai.sensor.domain.analytics;

import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * High-level executive dashboard summary for a field parcel combining latest readings,
 * 24-hour baseline aggregates, hardware availability, and data quality metrics.
 */
public record FieldAnalyticsSummaryData(
        UUID fieldId,
        Map<SensorType, SensorReading> latestReadings,
        Map<SensorType, TelemetryAggregate> past24hAggregates,
        SensorAvailabilitySummary availability,
        DataQualityMetrics past24hDataQuality,
        Instant generatedAt
) {

    public FieldAnalyticsSummaryData {
        Objects.requireNonNull(fieldId, "fieldId must not be null");
        Objects.requireNonNull(generatedAt, "generatedAt must not be null");

        latestReadings = latestReadings != null ? Map.copyOf(latestReadings) : Collections.emptyMap();
        past24hAggregates = past24hAggregates != null ? Map.copyOf(past24hAggregates) : Collections.emptyMap();
        availability = availability != null ? availability : SensorAvailabilitySummary.empty();
        past24hDataQuality = past24hDataQuality != null ? past24hDataQuality : DataQualityMetrics.calculate(0, 0, 0, 0, 0, 0);
    }
}
