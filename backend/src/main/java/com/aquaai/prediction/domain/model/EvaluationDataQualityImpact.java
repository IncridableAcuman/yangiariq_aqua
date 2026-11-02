package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;

/**
 * Accuracy and error breakdown correlated with input telemetry data quality.
 */
public record EvaluationDataQualityImpact(
        EvaluationDataQuality dataQuality,
        int sampleSize,
        boolean isSufficientSample,
        BigDecimal meanAbsoluteError,
        BigDecimal accuracyPercent,
        String descriptiveInsight
) {}
