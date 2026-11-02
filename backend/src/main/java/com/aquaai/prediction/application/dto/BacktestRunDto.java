package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.BacktestRun;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BacktestRunDto(
        UUID id,
        UUID proposalId,
        String currentVersionTag,
        String candidateVersionTag,
        Instant dateRangeFrom,
        Instant dateRangeTo,
        int sampleSize,
        BigDecimal currentMae,
        BigDecimal candidateMae,
        BigDecimal maeImprovementPercent,
        BigDecimal currentRmse,
        BigDecimal candidateRmse,
        BigDecimal currentMbe,
        BigDecimal candidateMbe,
        BigDecimal currentAccuracyPercent,
        BigDecimal candidateAccuracyPercent,
        BigDecimal currentF1,
        BigDecimal candidateF1,
        boolean regressionDetected,
        String regressionDetails,
        String dataQuality,
        boolean isComparable,
        boolean hasSufficientSample,
        Instant executedAt,
        String executedBy
) {
    public static BacktestRunDto fromDomain(BacktestRun b) {
        if (b == null) return null;
        return new BacktestRunDto(
                b.getId(),
                b.getProposalId(),
                b.getCurrentVersionTag(),
                b.getCandidateVersionTag(),
                b.getDateRangeFrom(),
                b.getDateRangeTo(),
                b.getSampleSize(),
                b.getCurrentMae(),
                b.getCandidateMae(),
                b.getMaeImprovementPercent(),
                b.getCurrentRmse(),
                b.getCandidateRmse(),
                b.getCurrentMbe(),
                b.getCandidateMbe(),
                b.getCurrentAccuracyPercent(),
                b.getCandidateAccuracyPercent(),
                b.getCurrentF1(),
                b.getCandidateF1(),
                b.isRegressionDetected(),
                b.getRegressionDetails(),
                b.getDataQuality().name(),
                b.isComparable(),
                b.isHasSufficientSample(),
                b.getExecutedAt(),
                b.getExecutedBy()
        );
    }
}
