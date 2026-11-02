package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;

/**
 * Performance breakdown for a confidence tier (e.g. HIGH >= 75%, MEDIUM 50-74%, LOW < 50%).
 */
public record EvaluationConfidenceTier(
        String tierName,
        int minConfidence,
        int maxConfidence,
        int sampleSize,
        boolean isSufficientSample,
        BigDecimal meanAbsoluteError,
        BigDecimal accuracyOrF1Percent,
        String calibrationStatus // CALIBRATED, INVERSE_CALIBRATION, UNVERIFIED
) {}
