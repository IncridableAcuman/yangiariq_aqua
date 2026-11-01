package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.analytics.DataQualityMetrics;

public record DataQualityMetricsResponse(
        long totalReadings,
        long validReadings,
        long suspectReadings,
        long invalidReadings,
        double validPercentage,
        long missingIntervals,
        long delayedReadings
) {

    public static DataQualityMetricsResponse from(DataQualityMetrics m) {
        if (m == null) {
            return null;
        }
        return new DataQualityMetricsResponse(
                m.totalReadings(),
                m.validReadings(),
                m.suspectReadings(),
                m.invalidReadings(),
                m.validPercentage(),
                m.missingIntervals(),
                m.delayedReadings()
        );
    }
}
