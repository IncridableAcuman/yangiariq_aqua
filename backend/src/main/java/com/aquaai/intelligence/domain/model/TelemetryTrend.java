package com.aquaai.intelligence.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * Lightweight temporal trend evaluation for a specific telemetry sensor modality.
 */
@Getter
public class TelemetryTrend {

    private final TrendDirection direction;
    private final BigDecimal rateOfChangePerHour;
    private final int dataPointsEvaluated;
    private final Instant windowStart;
    private final Instant windowEnd;

    public TelemetryTrend(
            TrendDirection direction,
            BigDecimal rateOfChangePerHour,
            int dataPointsEvaluated,
            Instant windowStart,
            Instant windowEnd
    ) {
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.rateOfChangePerHour = rateOfChangePerHour != null
                ? rateOfChangePerHour.setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.dataPointsEvaluated = Math.max(0, dataPointsEvaluated);
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    public static TelemetryTrend stable(int dataPoints, Instant start, Instant end) {
        return new TelemetryTrend(TrendDirection.STABLE, BigDecimal.ZERO, dataPoints, start, end);
    }
}
