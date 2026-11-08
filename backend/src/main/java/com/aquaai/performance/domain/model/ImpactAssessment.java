package com.aquaai.performance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain aggregate representing an operational outcome and impact assessment.
 * Evaluates whether an intervention produced measurable and verifiable regional improvement.
 */
@Getter
@Builder(toBuilder = true)
public class ImpactAssessment {

    private final UUID id;
    private final String assessmentNumber;
    private final String title;
    private final String interventionType;
    private final UUID interventionId;
    private final String interventionReference;
    private final Instant interventionDate;
    private final String indicatorCode;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final UUID baselineId;
    private final Instant baselinePeriodStart;
    private final Instant baselinePeriodEnd;
    private final BigDecimal baselineValue;
    private final Instant evaluationPeriodStart;
    private final Instant evaluationPeriodEnd;
    private final BigDecimal postInterventionValue;
    private final BigDecimal absoluteChange;
    private final BigDecimal percentageChange;
    private final ChangeDirection changeDirection;
    private final MeasurementUnit measurementUnit;
    private final EvidenceClassification evidenceClassification;
    private final BaselineQuality baselineQuality;
    private final AttributionLevel attributionLevel;
    private final String confoundingFactors;
    private final String dataLimitations;
    private final boolean comparisonValid;
    private final String methodologyNotes;
    private final AssessmentReviewStatus reviewStatus;
    private final UUID reviewerId;
    private final String reviewerName;
    private final String reviewNotes;
    private final Instant reviewedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    public ImpactAssessment(
            UUID id,
            String assessmentNumber,
            String title,
            String interventionType,
            UUID interventionId,
            String interventionReference,
            Instant interventionDate,
            String indicatorCode,
            GeographicScopeType scopeType,
            UUID scopeId,
            String scopeName,
            UUID baselineId,
            Instant baselinePeriodStart,
            Instant baselinePeriodEnd,
            BigDecimal baselineValue,
            Instant evaluationPeriodStart,
            Instant evaluationPeriodEnd,
            BigDecimal postInterventionValue,
            BigDecimal absoluteChange,
            BigDecimal percentageChange,
            ChangeDirection changeDirection,
            MeasurementUnit measurementUnit,
            EvidenceClassification evidenceClassification,
            BaselineQuality baselineQuality,
            AttributionLevel attributionLevel,
            String confoundingFactors,
            String dataLimitations,
            boolean comparisonValid,
            String methodologyNotes,
            AssessmentReviewStatus reviewStatus,
            UUID reviewerId,
            String reviewerName,
            String reviewNotes,
            Instant reviewedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.assessmentNumber = Objects.requireNonNull(assessmentNumber, "assessmentNumber must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.interventionType = Objects.requireNonNull(interventionType, "interventionType must not be null");
        this.interventionId = Objects.requireNonNull(interventionId, "interventionId must not be null");
        this.interventionReference = Objects.requireNonNull(interventionReference, "interventionReference must not be null");
        this.interventionDate = Objects.requireNonNull(interventionDate, "interventionDate must not be null");
        this.indicatorCode = Objects.requireNonNull(indicatorCode, "indicatorCode must not be null");
        this.scopeType = Objects.requireNonNull(scopeType, "scopeType must not be null");
        this.scopeId = scopeId;
        this.scopeName = Objects.requireNonNull(scopeName, "scopeName must not be null");
        this.baselineId = baselineId;
        this.baselinePeriodStart = Objects.requireNonNull(baselinePeriodStart, "baselinePeriodStart must not be null");
        this.baselinePeriodEnd = Objects.requireNonNull(baselinePeriodEnd, "baselinePeriodEnd must not be null");
        this.baselineValue = Objects.requireNonNull(baselineValue, "baselineValue must not be null");
        this.evaluationPeriodStart = Objects.requireNonNull(evaluationPeriodStart, "evaluationPeriodStart must not be null");
        this.evaluationPeriodEnd = Objects.requireNonNull(evaluationPeriodEnd, "evaluationPeriodEnd must not be null");
        this.postInterventionValue = Objects.requireNonNull(postInterventionValue, "postInterventionValue must not be null");
        this.absoluteChange = Objects.requireNonNull(absoluteChange, "absoluteChange must not be null");
        this.percentageChange = Objects.requireNonNull(percentageChange, "percentageChange must not be null");
        this.changeDirection = changeDirection != null ? changeDirection : ChangeDirection.FAVORABLE;
        this.measurementUnit = Objects.requireNonNull(measurementUnit, "measurementUnit must not be null");
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.baselineQuality = baselineQuality != null ? baselineQuality : BaselineQuality.VERIFIED_BASELINE;
        this.attributionLevel = attributionLevel != null ? attributionLevel : AttributionLevel.STRONGLY_ASSOCIATED;
        this.confoundingFactors = confoundingFactors;
        this.dataLimitations = dataLimitations;
        this.comparisonValid = comparisonValid;
        this.methodologyNotes = Objects.requireNonNull(methodologyNotes, "methodologyNotes must not be null");
        this.reviewStatus = reviewStatus != null ? reviewStatus : AssessmentReviewStatus.SUBMITTED;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewNotes = reviewNotes;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }
}
