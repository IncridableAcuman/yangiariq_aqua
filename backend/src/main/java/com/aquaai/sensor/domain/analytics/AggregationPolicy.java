package com.aquaai.sensor.domain.analytics;

import com.aquaai.sensor.domain.model.SensorType;

/**
 * Modality-specific mathematical aggregation policies for agricultural telemetry.
 * Prevents mathematically meaningless operations (e.g. summing soil moisture percentages or averaging accumulated rainfall).
 */
public final class AggregationPolicy {

    private AggregationPolicy() {
    }

    /**
     * Determines whether arithmetic averaging is mathematically meaningful for the sensor modality.
     */
    public static boolean isAverageMeaningful(SensorType sensorType) {
        if (sensorType == null) {
            return false;
        }
        // Rainfall is an accumulation/rate metric; average of instantaneous increments is not meaningful.
        return sensorType != SensorType.RAINFALL;
    }

    /**
     * Determines whether arithmetic summation is mathematically meaningful for the sensor modality.
     */
    public static boolean isSumMeaningful(SensorType sensorType) {
        if (sensorType == null) {
            return false;
        }
        // Only precipitation accumulation is additive across time buckets.
        return sensorType == SensorType.RAINFALL;
    }

    /**
     * Determines whether finding minimum and maximum bounds is mathematically meaningful.
     */
    public static boolean isMinMaxMeaningful(SensorType sensorType) {
        return sensorType != null;
    }
}
