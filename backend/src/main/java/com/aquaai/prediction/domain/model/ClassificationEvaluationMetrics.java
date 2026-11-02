package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;

/**
 * Standard classification forecast metrics (Confusion Matrix, Accuracy, Precision, Recall, F1).
 * Adheres strictly to the minimum sample size policy.
 */
public record ClassificationEvaluationMetrics(
        int sampleSize,
        int minimumSampleSizeRequired,
        boolean isSufficientSample,
        int truePositives,
        int trueNegatives,
        int falsePositives,
        int falseNegatives,
        int interruptedCount,
        BigDecimal accuracyPercent,
        BigDecimal precisionPercent,
        BigDecimal recallPercent,
        BigDecimal f1ScorePercent
) {
    public static ClassificationEvaluationMetrics empty(int minSample) {
        return new ClassificationEvaluationMetrics(
                0,
                minSample,
                false,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                null,
                null
        );
    }
}
