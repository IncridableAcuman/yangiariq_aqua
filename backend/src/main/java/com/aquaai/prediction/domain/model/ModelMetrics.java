package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standardized regression evaluation metrics calculated on a specific evaluation split.
 */
public record ModelMetrics(
        double mae,
        double rmse,
        double mbe,
        Double mape,
        double rSquared,
        int sampleSize,
        int confidencePercent,
        double withinBoundsRate
) {
    public static ModelMetrics empty(int sampleSize) {
        return new ModelMetrics(0.0, 0.0, 0.0, null, 0.0, sampleSize, 0, 0.0);
    }

    public BigDecimal toBigDecimalMae() {
        return BigDecimal.valueOf(mae).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal toBigDecimalRmse() {
        return BigDecimal.valueOf(rmse).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal toBigDecimalMbe() {
        return BigDecimal.valueOf(mbe).setScale(4, RoundingMode.HALF_UP);
    }
}
