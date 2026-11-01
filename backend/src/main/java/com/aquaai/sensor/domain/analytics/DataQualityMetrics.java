package com.aquaai.sensor.domain.analytics;

/**
 * Analytical metrics evaluating telemetry data health, validity, and reporting consistency.
 */
public record DataQualityMetrics(
        long totalReadings,
        long validReadings,
        long suspectReadings,
        long invalidReadings,
        double validPercentage,
        long missingIntervals,
        long delayedReadings
) {

    public DataQualityMetrics {
        if (totalReadings < 0 || validReadings < 0 || suspectReadings < 0 || invalidReadings < 0) {
            throw new IllegalArgumentException("Reading counts cannot be negative");
        }
        if (validPercentage < 0.0 || validPercentage > 100.0) {
            throw new IllegalArgumentException("Valid percentage must be between 0.0 and 100.0");
        }
    }

    public static DataQualityMetrics calculate(
            long total,
            long valid,
            long suspect,
            long invalid,
            long missing,
            long delayed
    ) {
        double percentage = total > 0 ? ((double) valid / (double) total) * 100.0 : 100.0;
        // round to 2 decimal places
        double rounded = Math.round(percentage * 100.0) / 100.0;
        return new DataQualityMetrics(total, valid, suspect, invalid, rounded, missing, delayed);
    }
}
