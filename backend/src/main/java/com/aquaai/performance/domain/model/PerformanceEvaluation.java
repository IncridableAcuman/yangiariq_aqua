package com.aquaai.performance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain aggregate representing an immutable historical performance evaluation snapshot.
 */
@Getter
@Builder(toBuilder = true)
public class PerformanceEvaluation {

    private final UUID id;
    private final String evaluationNumber;
    private final String indicatorCode;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal calculatedValue;
    private final MeasurementUnit measurementUnit;
    private final UUID baselineId;
    private final BigDecimal baselineValue;
    private final UUID targetId;
    private final BigDecimal targetValue;
    private final TargetComplianceStatus targetCompliance;
    private final BigDecimal varianceFromBaselinePct;
    private final BigDecimal varianceFromTargetPct;
    private final PerformanceTrend trend;
    private final EvidenceClassification evidenceClassification;
    private final BigDecimal observedSampleRatio;
    private final int missingDataIntervals;
    private final boolean comparisonValid;
    private final String validityNotes;
    private final String explanatorySummary;
    private final String calculationVersion;
    private final Instant evaluatedAt;

    public PerformanceEvaluation(
            UUID id,
            String evaluationNumber,
            String indicatorCode,
            GeographicScopeType scopeType,
            UUID scopeId,
            String scopeName,
            Instant periodStart,
            Instant periodEnd,
            BigDecimal calculatedValue,
            MeasurementUnit measurementUnit,
            UUID baselineId,
            BigDecimal baselineValue,
            UUID targetId,
            BigDecimal targetValue,
            TargetComplianceStatus targetCompliance,
            BigDecimal varianceFromBaselinePct,
            BigDecimal varianceFromTargetPct,
            PerformanceTrend trend,
            EvidenceClassification evidenceClassification,
            BigDecimal observedSampleRatio,
            int missingDataIntervals,
            boolean comparisonValid,
            String validityNotes,
            String explanatorySummary,
            String calculationVersion,
            Instant evaluatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.evaluationNumber = Objects.requireNonNull(evaluationNumber, "evaluationNumber must not be null");
        this.indicatorCode = Objects.requireNonNull(indicatorCode, "indicatorCode must not be null");
        this.scopeType = Objects.requireNonNull(scopeType, "scopeType must not be null");
        this.scopeId = scopeId;
        this.scopeName = Objects.requireNonNull(scopeName, "scopeName must not be null");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart must not be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd must not be null");
        this.calculatedValue = Objects.requireNonNull(calculatedValue, "calculatedValue must not be null");
        this.measurementUnit = Objects.requireNonNull(measurementUnit, "measurementUnit must not be null");
        this.baselineId = baselineId;
        this.baselineValue = baselineValue;
        this.targetId = targetId;
        this.targetValue = targetValue;
        this.targetCompliance = targetCompliance != null ? targetCompliance : TargetComplianceStatus.MET;
        this.varianceFromBaselinePct = varianceFromBaselinePct;
        this.varianceFromTargetPct = varianceFromTargetPct;
        this.trend = trend != null ? trend : PerformanceTrend.STABLE;
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.observedSampleRatio = observedSampleRatio != null ? observedSampleRatio : new BigDecimal("100.00");
        this.missingDataIntervals = missingDataIntervals;
        this.comparisonValid = comparisonValid;
        this.validityNotes = validityNotes;
        this.explanatorySummary = Objects.requireNonNull(explanatorySummary, "explanatorySummary must not be null");
        this.calculationVersion = calculationVersion != null ? calculationVersion : "v1.0.0";
        this.evaluatedAt = evaluatedAt != null ? evaluatedAt : Instant.now();
    }
}
