package com.aquaai.sensor.domain.analytics;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Standardized time-series aggregation intervals for agricultural telemetry.
 */
public enum AggregationInterval {

    FIVE_MINUTES("5m", Duration.ofMinutes(5), 7),
    FIFTEEN_MINUTES("15m", Duration.ofMinutes(15), 30),
    HOURLY("1h", Duration.ofHours(1), 90),
    DAILY("1d", Duration.ofDays(1), 365),
    WEEKLY("1w", Duration.ofDays(7), 730);

    private final String code;
    private final Duration duration;
    private final int maxAllowedQueryDays;

    AggregationInterval(String code, Duration duration, int maxAllowedQueryDays) {
        this.code = code;
        this.duration = duration;
        this.maxAllowedQueryDays = maxAllowedQueryDays;
    }

    public String getCode() {
        return code;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getMaxAllowedQueryDays() {
        return maxAllowedQueryDays;
    }

    /**
     * Parses an interval code (e.g. "5m", "15m", "1h", "1d", "1w"), defaulting to HOURLY if null or blank.
     */
    public static AggregationInterval fromCode(String code) {
        if (code == null || code.isBlank()) {
            return HOURLY;
        }
        String clean = code.trim().toLowerCase();
        for (AggregationInterval interval : values()) {
            if (interval.code.equalsIgnoreCase(clean) || interval.name().equalsIgnoreCase(clean)) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Unknown aggregation interval: '" + code + "'. Supported: 5m, 15m, 1h, 1d, 1w");
    }

    /**
     * Truncates an instant down to this interval's start boundary in UTC.
     */
    public Instant truncate(Instant instant) {
        if (instant == null) {
            return null;
        }
        long epochSeconds = instant.getEpochSecond();
        long intervalSeconds = duration.getSeconds();
        long bucketStartSeconds = (epochSeconds / intervalSeconds) * intervalSeconds;
        return Instant.ofEpochSecond(bucketStartSeconds);
    }
}
