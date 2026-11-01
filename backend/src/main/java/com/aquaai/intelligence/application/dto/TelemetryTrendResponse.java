package com.aquaai.intelligence.application.dto;

import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.model.TrendDirection;

import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryTrendResponse(
        TrendDirection direction,
        BigDecimal rateOfChangePerHour,
        int dataPointsEvaluated,
        Instant windowStart,
        Instant windowEnd
) {
    public static TelemetryTrendResponse fromDomain(TelemetryTrend domain) {
        if (domain == null) return null;
        return new TelemetryTrendResponse(
                domain.getDirection(),
                domain.getRateOfChangePerHour(),
                domain.getDataPointsEvaluated(),
                domain.getWindowStart(),
                domain.getWindowEnd()
        );
    }
}
