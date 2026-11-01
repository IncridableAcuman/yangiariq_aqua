package com.aquaai.recommendation.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Point-in-time and near-term forecast meteorological snapshot for a field parcel.
 */
@Getter
@Builder
public class WeatherSnapshot {

    private final BigDecimal temperature;
    private final BigDecimal relativeHumidity;
    private final BigDecimal precipitationProbability;
    private final BigDecimal precipitationAmountMm;
    private final BigDecimal windSpeedMps;
    private final BigDecimal atmosphericPressureHpa;
    private final Instant timestamp;
    private final String source;

    public boolean isFresh(Instant now, int maxStaleMinutes) {
        if (timestamp == null || now == null) {
            return false;
        }
        long minutesOld = Math.abs(Duration.between(timestamp, now).toMinutes());
        return minutesOld <= maxStaleMinutes;
    }

    public boolean hasSignificantRainfall(BigDecimal thresholdMm) {
        if (precipitationAmountMm == null) {
            return false;
        }
        return precipitationAmountMm.compareTo(Objects.requireNonNullElse(thresholdMm, BigDecimal.valueOf(10.0))) >= 0;
    }

    public boolean isExtremeHeat(BigDecimal thresholdCelsius) {
        if (temperature == null) {
            return false;
        }
        return temperature.compareTo(Objects.requireNonNullElse(thresholdCelsius, BigDecimal.valueOf(40.0))) >= 0;
    }

    public boolean isFrostRisk(BigDecimal thresholdCelsius) {
        if (temperature == null) {
            return false;
        }
        return temperature.compareTo(Objects.requireNonNullElse(thresholdCelsius, BigDecimal.valueOf(2.0))) <= 0;
    }
}
