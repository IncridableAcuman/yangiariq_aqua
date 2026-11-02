package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.CandidateComparisonResult;

import java.math.BigDecimal;

public record CandidateComparisonDto(
        String predictionType,
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
    public static CandidateComparisonDto fromDomain(CandidateComparisonResult c) {
        if (c == null) return null;
        return new CandidateComparisonDto(
                c.predictionType().name(),
                c.currentVersionTag(),
                c.candidateVersionTag(),
                c.sampleSize(),
                c.currentMae(),
                c.candidateMae(),
                c.maeDeltaPercent(),
                c.currentRmse(),
                c.candidateRmse(),
                c.currentMbe(),
                c.candidateMbe(),
                c.currentAccuracyPercent(),
                c.candidateAccuracyPercent(),
                c.currentF1(),
                c.candidateF1(),
                c.regressionDetected(),
                c.regressionWarning(),
                c.improvementSummary(),
                c.isReadyForApproval()
        );
    }
}
