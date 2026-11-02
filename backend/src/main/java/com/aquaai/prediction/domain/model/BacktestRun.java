package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable historical backtest run executing candidate version logic against identical historical
 * observations with strict anti-leakage protection.
 */
public class BacktestRun {

    private final UUID id;
    private final UUID proposalId;
    private final String currentVersionTag;
    private final String candidateVersionTag;
    private final Instant dateRangeFrom;
    private final Instant dateRangeTo;
    private final int sampleSize;
    private final BigDecimal currentMae;
    private final BigDecimal candidateMae;
    private final BigDecimal maeImprovementPercent;
    private final BigDecimal currentRmse;
    private final BigDecimal candidateRmse;
    private final BigDecimal currentMbe;
    private final BigDecimal candidateMbe;
    private final BigDecimal currentAccuracyPercent;
    private final BigDecimal candidateAccuracyPercent;
    private final BigDecimal currentF1;
    private final BigDecimal candidateF1;
    private final boolean regressionDetected;
    private final String regressionDetails;
    private final EvaluationDataQuality dataQuality;
    private final boolean isComparable;
    private final boolean hasSufficientSample;
    private final Instant executedAt;
    private final String executedBy;

    public BacktestRun(
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
            EvaluationDataQuality dataQuality,
            boolean isComparable,
            boolean hasSufficientSample,
            Instant executedAt,
            String executedBy
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.proposalId = Objects.requireNonNull(proposalId, "proposalId must not be null");
        this.currentVersionTag = Objects.requireNonNull(currentVersionTag, "currentVersionTag must not be null");
        this.candidateVersionTag = Objects.requireNonNull(candidateVersionTag, "candidateVersionTag must not be null");
        this.dateRangeFrom = Objects.requireNonNull(dateRangeFrom, "dateRangeFrom must not be null");
        this.dateRangeTo = Objects.requireNonNull(dateRangeTo, "dateRangeTo must not be null");
        this.sampleSize = sampleSize;
        this.currentMae = currentMae;
        this.candidateMae = candidateMae;
        this.maeImprovementPercent = maeImprovementPercent;
        this.currentRmse = currentRmse;
        this.candidateRmse = candidateRmse;
        this.currentMbe = currentMbe;
        this.candidateMbe = candidateMbe;
        this.currentAccuracyPercent = currentAccuracyPercent;
        this.candidateAccuracyPercent = candidateAccuracyPercent;
        this.currentF1 = currentF1;
        this.candidateF1 = candidateF1;
        this.regressionDetected = regressionDetected;
        this.regressionDetails = regressionDetails != null ? regressionDetails : "";
        this.dataQuality = dataQuality != null ? dataQuality : EvaluationDataQuality.DATA_COMPLETE;
        this.isComparable = isComparable;
        this.hasSufficientSample = hasSufficientSample;
        this.executedAt = executedAt != null ? executedAt : Instant.now();
        this.executedBy = executedBy != null ? executedBy : "SYSTEM";
    }

    public static BigDecimal calculateImprovement(BigDecimal baseline, BigDecimal candidate) {
        if (baseline == null || candidate == null || baseline.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // (baseline - candidate) / baseline * 100
        return baseline.subtract(candidate)
                .divide(baseline.abs(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getProposalId() { return proposalId; }
    public String getCurrentVersionTag() { return currentVersionTag; }
    public String getCandidateVersionTag() { return candidateVersionTag; }
    public Instant getDateRangeFrom() { return dateRangeFrom; }
    public Instant getDateRangeTo() { return dateRangeTo; }
    public int getSampleSize() { return sampleSize; }
    public BigDecimal getCurrentMae() { return currentMae; }
    public BigDecimal getCandidateMae() { return candidateMae; }
    public BigDecimal getMaeImprovementPercent() { return maeImprovementPercent; }
    public BigDecimal getCurrentRmse() { return currentRmse; }
    public BigDecimal getCandidateRmse() { return candidateRmse; }
    public BigDecimal getCurrentMbe() { return currentMbe; }
    public BigDecimal getCandidateMbe() { return candidateMbe; }
    public BigDecimal getCurrentAccuracyPercent() { return currentAccuracyPercent; }
    public BigDecimal getCandidateAccuracyPercent() { return candidateAccuracyPercent; }
    public BigDecimal getCurrentF1() { return currentF1; }
    public BigDecimal getCandidateF1() { return candidateF1; }
    public boolean isRegressionDetected() { return regressionDetected; }
    public String getRegressionDetails() { return regressionDetails; }
    public EvaluationDataQuality getDataQuality() { return dataQuality; }
    public boolean isComparable() { return isComparable; }
    public boolean isHasSufficientSample() { return hasSufficientSample; }
    public Instant getExecutedAt() { return executedAt; }
    public String getExecutedBy() { return executedBy; }
}
