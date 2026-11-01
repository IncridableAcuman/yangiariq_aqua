package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.analytics.FieldAnalyticsSummaryData;
import com.aquaai.sensor.domain.model.SensorType;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record FieldAnalyticsSummaryResponse(
        UUID fieldId,
        Map<SensorType, SensorReadingResponse> latestReadings,
        Map<SensorType, TelemetryAggregateResponse> past24hAggregates,
        SensorAvailabilityResponse availability,
        DataQualityMetricsResponse past24hDataQuality,
        Instant generatedAt
) {

    public static FieldAnalyticsSummaryResponse from(FieldAnalyticsSummaryData data) {
        if (data == null) {
            return null;
        }

        Map<SensorType, SensorReadingResponse> latestMap = new HashMap<>();
        if (data.latestReadings() != null) {
            data.latestReadings().forEach((type, reading) ->
                    latestMap.put(type, SensorReadingResponse.from(reading)));
        }

        Map<SensorType, TelemetryAggregateResponse> aggregatesMap = new HashMap<>();
        if (data.past24hAggregates() != null) {
            data.past24hAggregates().forEach((type, agg) ->
                    aggregatesMap.put(type, TelemetryAggregateResponse.from(agg)));
        }

        return new FieldAnalyticsSummaryResponse(
                data.fieldId(),
                Collections.unmodifiableMap(latestMap),
                Collections.unmodifiableMap(aggregatesMap),
                SensorAvailabilityResponse.from(data.availability()),
                DataQualityMetricsResponse.from(data.past24hDataQuality()),
                data.generatedAt()
        );
    }
}
