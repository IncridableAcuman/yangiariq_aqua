package com.aquaai.intelligence.domain.service;

import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.model.TrendDirection;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Deterministic temporal trend evaluator for sensor readings.
 */
public class TemporalTrendCalculator {

    public record DataPoint(BigDecimal value, Instant measuredAt) {}

    private static final BigDecimal EPSILON_PER_HOUR = new BigDecimal("0.2000");

    public TelemetryTrend calculateTrend(List<DataPoint> dataPoints) {
        if (dataPoints == null || dataPoints.size() < 2) {
            int count = dataPoints != null ? dataPoints.size() : 0;
            Instant start = (count > 0) ? dataPoints.get(0).measuredAt() : null;
            Instant end = (count > 0) ? dataPoints.get(count - 1).measuredAt() : null;
            return new TelemetryTrend(TrendDirection.STABLE, BigDecimal.ZERO, count, start, end);
        }

        List<DataPoint> sorted = dataPoints.stream()
                .filter(p -> p != null && p.value() != null && p.measuredAt() != null)
                .sorted(Comparator.comparing(DataPoint::measuredAt))
                .toList();

        if (sorted.size() < 2) {
            return new TelemetryTrend(TrendDirection.STABLE, BigDecimal.ZERO, sorted.size(), null, null);
        }

        DataPoint first = sorted.get(0);
        DataPoint last = sorted.get(sorted.size() - 1);

        Duration elapsed = Duration.between(first.measuredAt(), last.measuredAt());
        long seconds = elapsed.getSeconds();

        // If span is less than 5 minutes, rate per hour is unstable; consider stable
        if (seconds < 300) {
            return new TelemetryTrend(TrendDirection.STABLE, BigDecimal.ZERO, sorted.size(), first.measuredAt(), last.measuredAt());
        }

        BigDecimal deltaValue = last.value().subtract(first.value());
        BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), MathContext.DECIMAL64);

        BigDecimal ratePerHour = deltaValue.divide(hours, 4, RoundingMode.HALF_UP);

        TrendDirection direction;
        if (ratePerHour.compareTo(EPSILON_PER_HOUR) > 0) {
            direction = TrendDirection.INCREASING;
        } else if (ratePerHour.compareTo(EPSILON_PER_HOUR.negate()) < 0) {
            direction = TrendDirection.DECREASING;
        } else {
            direction = TrendDirection.STABLE;
        }

        return new TelemetryTrend(direction, ratePerHour, sorted.size(), first.measuredAt(), last.measuredAt());
    }
}
