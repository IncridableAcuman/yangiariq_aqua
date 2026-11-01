package com.aquaai.intelligence.domain.service;

import com.aquaai.intelligence.domain.model.TelemetryFreshness;
import com.aquaai.sensor.domain.model.SensorType;

import java.time.Duration;
import java.time.Instant;

/**
 * Deterministic service evaluating telemetry freshness based on engineering SLA policies.
 */
public class FreshnessEvaluator {

    public TelemetryFreshness evaluate(SensorType type, Instant measuredAt, Instant now) {
        if (measuredAt == null) {
            return TelemetryFreshness.MISSING;
        }

        Instant current = now != null ? now : Instant.now();
        Duration elapsed = Duration.between(measuredAt, current);

        if (elapsed.isNegative()) {
            return TelemetryFreshness.FRESH;
        }

        long minutes = elapsed.toMinutes();

        if (type == null) {
            return defaultFreshness(minutes);
        }

        return switch (type) {
            case SOIL_MOISTURE -> {
                if (minutes <= 360) yield TelemetryFreshness.FRESH;         // <= 6 hours
                if (minutes <= 1440) yield TelemetryFreshness.STALE;        // <= 24 hours
                yield TelemetryFreshness.MISSING;
            }
            case TEMPERATURE, HUMIDITY -> {
                if (minutes <= 120) yield TelemetryFreshness.FRESH;         // <= 2 hours
                if (minutes <= 720) yield TelemetryFreshness.STALE;         // <= 12 hours
                yield TelemetryFreshness.MISSING;
            }
            case PRESSURE, WATER_FLOW -> {
                if (minutes <= 30) yield TelemetryFreshness.FRESH;          // <= 30 minutes
                if (minutes <= 120) yield TelemetryFreshness.STALE;         // <= 2 hours
                yield TelemetryFreshness.MISSING;
            }
            case WATER_LEVEL, SALINITY, RAINFALL -> {
                if (minutes <= 360) yield TelemetryFreshness.FRESH;         // <= 6 hours
                if (minutes <= 1440) yield TelemetryFreshness.STALE;        // <= 24 hours
                yield TelemetryFreshness.MISSING;
            }
        };
    }

    private TelemetryFreshness defaultFreshness(long minutes) {
        if (minutes <= 120) return TelemetryFreshness.FRESH;
        if (minutes <= 1440) return TelemetryFreshness.STALE;
        return TelemetryFreshness.MISSING;
    }
}
