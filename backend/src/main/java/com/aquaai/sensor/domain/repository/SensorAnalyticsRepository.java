package com.aquaai.sensor.domain.repository;

import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.DataQualityMetrics;
import com.aquaai.sensor.domain.analytics.SensorAvailabilitySummary;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain port for time-series analytics, time-window aggregations, and sensor fleet metrics.
 */
public interface SensorAnalyticsRepository {

    /**
     * Aggregates telemetry readings for a specific sensor over a designated interval.
     */
    List<TelemetryAggregate> aggregateBySensor(
            UUID sensorId,
            Instant from,
            Instant to,
            AggregationInterval interval,
            ReadingQuality qualityFilter
    );

    /**
     * Aggregates telemetry readings across all sensors of a given modality for a designated field parcel.
     */
    List<TelemetryAggregate> aggregateByField(
            UUID fieldId,
            SensorType sensorType,
            Instant from,
            Instant to,
            AggregationInterval interval,
            ReadingQuality qualityFilter
    );

    /**
     * Computes data-quality metrics (valid, suspect, invalid, delayed readings) over a time window.
     */
    DataQualityMetrics calculateDataQuality(
            UUID sensorId,
            UUID fieldId,
            Instant from,
            Instant to
    );

    /**
     * Calculates sensor lifecycle status and communication availability across a designated field parcel.
     */
    SensorAvailabilitySummary calculateAvailability(
            UUID fieldId,
            Duration offlineThreshold
    );
}
