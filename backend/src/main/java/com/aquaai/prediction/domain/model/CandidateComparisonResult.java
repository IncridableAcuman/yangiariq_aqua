package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;

/**
 * Detailed explainable comparison between current active version and candidate version.
 */
public record CandidateComparisonResult(
        PredictionType predictionType,
        String currentVersionTag,
        String candidateVersionTag,
        int sampleSize,
        BigDecimal currentMae,
        BigDecimal candidateMae,
        BigDecimal maeDeltaPercent,
        BigDecimal currentRmse,
        BigDecimal candidateRmse,
        BigDecimal currentMbe,
        BigDecimal candidateMbe,
        BigDecimal currentAccuracyPercent,
        BigDecimal candidateAccuracyPercent,
        BigDecimal currentF1,
        BigDecimal candidateF1,
        boolean regressionDetected,
        String regressionWarning,
        String improvementSummary,
        boolean isReadyForApproval
) {
}
