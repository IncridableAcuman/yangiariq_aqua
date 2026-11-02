package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;

/**
 * Standard numerical forecast metrics (MAE, RMSE, safe MAPE, Mean Bias Error).
 * Adheres strictly to the minimum sample size policy.
 */
public record NumericalEvaluationMetrics(
        int sampleSize,
        int minimumSampleSizeRequired,
        boolean isSufficientSample,
        BigDecimal meanAbsoluteError, // MAE
        BigDecimal rootMeanSquaredError, // RMSE
        BigDecimal meanAbsolutePercentageError, // MAPE (null if zero actuals encountered)
        boolean isMapeApplicable,
        BigDecimal meanBiasError, // MBE (positive = overpredict, negative = underpredict)
        int withinBoundsCount,
        BigDecimal withinBoundsPercentage
) {
    public static NumericalEvaluationMetrics empty(int minSample) {
        return new NumericalEvaluationMetrics(
                0,
                minSample,
                false,
                null,
                null,
                null,
                false,
                null,
                0,
                BigDecimal.ZERO
        );
    }
}
