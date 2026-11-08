package com.aquaai.performance.domain.port;

import java.math.BigDecimal;

public interface PerformancePredictionPort {

    record PredictionPerformanceSummary(
            long totalEvaluatedPredictions,
            BigDecimal meanAbsoluteError,
            BigDecimal forecastAccuracyPct,
            long approvedCalibrationsCount,
            BigDecimal humanAdoptionRatePct
    ) {}

    PredictionPerformanceSummary getPredictionPerformanceSummary();
}
