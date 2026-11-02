package com.aquaai.prediction.domain.model;

import java.time.Duration;

/**
 * Supported predictive time horizons with documented operational semantics.
 */
public enum PredictionHorizon {
    NEXT_24_HOURS(24, "Next 24 Hours", Duration.ofHours(24)),
    NEXT_48_HOURS(48, "Next 48 Hours", Duration.ofHours(48)),
    NEXT_7_DAYS(168, "Next 7 Days", Duration.ofDays(7));

    private final int hours;
    private final String label;
    private final Duration duration;

    PredictionHorizon(int hours, String label, Duration duration) {
        this.hours = hours;
        this.label = label;
        this.duration = duration;
    }

    public int getHours() {
        return hours;
    }

    public String getLabel() {
        return label;
    }

    public Duration getDuration() {
        return duration;
    }

    public static PredictionHorizon fromString(String value) {
        if (value == null || value.isBlank()) {
            return NEXT_48_HOURS;
        }
        for (PredictionHorizon horizon : values()) {
            if (horizon.name().equalsIgnoreCase(value) ||
                horizon.name().replace("_", "").equalsIgnoreCase(value.replace("_", ""))) {
                return horizon;
            }
        }
        return NEXT_48_HOURS;
    }
}
