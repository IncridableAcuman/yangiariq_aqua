package com.aquaai.performance.infrastructure.persistence.mapper;

import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.infrastructure.persistence.entity.ImpactAssessmentJpaEntity;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceBaselineJpaEntity;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceEvaluationJpaEntity;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceIndicatorJpaEntity;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceTargetJpaEntity;

public final class PerformanceJpaMapper {

    private PerformanceJpaMapper() {}

    public static PerformanceIndicator toDomain(PerformanceIndicatorJpaEntity entity) {
        if (entity == null) return null;
        return PerformanceIndicator.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .measurementUnit(entity.getMeasurementUnit())
                .calculationMethod(entity.getCalculationMethod())
                .aggregationMethod(entity.getAggregationMethod())
                .supportedGeographicScope(entity.getSupportedGeographicScope())
                .evidenceRequirement(entity.getEvidenceRequirement())
                .minimumDataQualityPct(entity.getMinimumDataQualityPct())
                .applicableTimePeriod(entity.getApplicableTimePeriod())
                .sourceModule(entity.getSourceModule())
                .calculationVersion(entity.getCalculationVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static PerformanceIndicatorJpaEntity toEntity(PerformanceIndicator domain) {
        if (domain == null) return null;
        return PerformanceIndicatorJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .measurementUnit(domain.getMeasurementUnit())
                .calculationMethod(domain.getCalculationMethod())
                .aggregationMethod(domain.getAggregationMethod())
                .supportedGeographicScope(domain.getSupportedGeographicScope())
                .evidenceRequirement(domain.getEvidenceRequirement())
                .minimumDataQualityPct(domain.getMinimumDataQualityPct())
                .applicableTimePeriod(domain.getApplicableTimePeriod())
                .sourceModule(domain.getSourceModule())
                .calculationVersion(domain.getCalculationVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static PerformanceBaseline toDomain(PerformanceBaselineJpaEntity entity) {
        if (entity == null) return null;
        return PerformanceBaseline.builder()
                .id(entity.getId())
                .indicatorCode(entity.getIndicatorCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .scopeType(entity.getScopeType())
                .scopeId(entity.getScopeId())
                .scopeName(entity.getScopeName())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .baselineValue(entity.getBaselineValue())
                .measurementUnit(entity.getMeasurementUnit())
                .dataCompletenessPct(entity.getDataCompletenessPct())
                .evidenceClassification(entity.getEvidenceClassification())
                .baselineQuality(entity.getBaselineQuality())
                .calculationMethod(entity.getCalculationMethod())
                .seasonContext(entity.getSeasonContext())
                .approvalStatus(entity.getApprovalStatus())
                .approvedBy(entity.getApprovedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static PerformanceBaselineJpaEntity toEntity(PerformanceBaseline domain) {
        if (domain == null) return null;
        return PerformanceBaselineJpaEntity.builder()
                .id(domain.getId())
                .indicatorCode(domain.getIndicatorCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .scopeType(domain.getScopeType())
                .scopeId(domain.getScopeId())
                .scopeName(domain.getScopeName())
                .periodStart(domain.getPeriodStart())
                .periodEnd(domain.getPeriodEnd())
                .baselineValue(domain.getBaselineValue())
                .measurementUnit(domain.getMeasurementUnit())
                .dataCompletenessPct(domain.getDataCompletenessPct())
                .evidenceClassification(domain.getEvidenceClassification())
                .baselineQuality(domain.getBaselineQuality())
                .calculationMethod(domain.getCalculationMethod())
                .seasonContext(domain.getSeasonContext())
                .approvalStatus(domain.getApprovalStatus())
                .approvedBy(domain.getApprovedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static PerformanceTarget toDomain(PerformanceTargetJpaEntity entity) {
        if (entity == null) return null;
        return PerformanceTarget.builder()
                .id(entity.getId())
                .indicatorCode(entity.getIndicatorCode())
                .title(entity.getTitle())
                .scopeType(entity.getScopeType())
                .scopeId(entity.getScopeId())
                .scopeName(entity.getScopeName())
                .targetType(entity.getTargetType())
                .targetCategory(entity.getTargetCategory())
                .targetValue(entity.getTargetValue())
                .rangeMin(entity.getRangeMin())
                .rangeMax(entity.getRangeMax())
                .measurementUnit(entity.getMeasurementUnit())
                .effectiveStart(entity.getEffectiveStart())
                .effectiveEnd(entity.getEffectiveEnd())
                .targetOwner(entity.getTargetOwner())
                .status(entity.getStatus())
                .rationale(entity.getRationale())
                .targetVersion(entity.getTargetVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static PerformanceTargetJpaEntity toEntity(PerformanceTarget domain) {
        if (domain == null) return null;
        return PerformanceTargetJpaEntity.builder()
                .id(domain.getId())
                .indicatorCode(domain.getIndicatorCode())
                .title(domain.getTitle())
                .scopeType(domain.getScopeType())
                .scopeId(domain.getScopeId())
                .scopeName(domain.getScopeName())
                .targetType(domain.getTargetType())
                .targetCategory(domain.getTargetCategory())
                .targetValue(domain.getTargetValue())
                .rangeMin(domain.getRangeMin())
                .rangeMax(domain.getRangeMax())
                .measurementUnit(domain.getMeasurementUnit())
                .effectiveStart(domain.getEffectiveStart())
                .effectiveEnd(domain.getEffectiveEnd())
                .targetOwner(domain.getTargetOwner())
                .status(domain.getStatus())
                .rationale(domain.getRationale())
                .targetVersion(domain.getTargetVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static PerformanceEvaluation toDomain(PerformanceEvaluationJpaEntity entity) {
        if (entity == null) return null;
        return PerformanceEvaluation.builder()
                .id(entity.getId())
                .evaluationNumber(entity.getEvaluationNumber())
                .indicatorCode(entity.getIndicatorCode())
                .scopeType(entity.getScopeType())
                .scopeId(entity.getScopeId())
                .scopeName(entity.getScopeName())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .calculatedValue(entity.getCalculatedValue())
                .measurementUnit(entity.getMeasurementUnit())
                .baselineId(entity.getBaselineId())
                .baselineValue(entity.getBaselineValue())
                .targetId(entity.getTargetId())
                .targetValue(entity.getTargetValue())
                .targetCompliance(entity.getTargetCompliance())
                .varianceFromBaselinePct(entity.getVarianceFromBaselinePct())
                .varianceFromTargetPct(entity.getVarianceFromTargetPct())
                .trend(entity.getTrend())
                .evidenceClassification(entity.getEvidenceClassification())
                .observedSampleRatio(entity.getObservedSampleRatio())
                .missingDataIntervals(entity.getMissingDataIntervals())
                .comparisonValid(entity.isComparisonValid())
                .validityNotes(entity.getValidityNotes())
                .explanatorySummary(entity.getExplanatorySummary())
                .calculationVersion(entity.getCalculationVersion())
                .evaluatedAt(entity.getEvaluatedAt())
                .build();
    }

    public static PerformanceEvaluationJpaEntity toEntity(PerformanceEvaluation domain) {
        if (domain == null) return null;
        return PerformanceEvaluationJpaEntity.builder()
                .id(domain.getId())
                .evaluationNumber(domain.getEvaluationNumber())
                .indicatorCode(domain.getIndicatorCode())
                .scopeType(domain.getScopeType())
                .scopeId(domain.getScopeId())
                .scopeName(domain.getScopeName())
                .periodStart(domain.getPeriodStart())
                .periodEnd(domain.getPeriodEnd())
                .calculatedValue(domain.getCalculatedValue())
                .measurementUnit(domain.getMeasurementUnit())
                .baselineId(domain.getBaselineId())
                .baselineValue(domain.getBaselineValue())
                .targetId(domain.getTargetId())
                .targetValue(domain.getTargetValue())
                .targetCompliance(domain.getTargetCompliance())
                .varianceFromBaselinePct(domain.getVarianceFromBaselinePct())
                .varianceFromTargetPct(domain.getVarianceFromTargetPct())
                .trend(domain.getTrend())
                .evidenceClassification(domain.getEvidenceClassification())
                .observedSampleRatio(domain.getObservedSampleRatio())
                .missingDataIntervals(domain.getMissingDataIntervals())
                .comparisonValid(domain.isComparisonValid())
                .validityNotes(domain.getValidityNotes())
                .explanatorySummary(domain.getExplanatorySummary())
                .calculationVersion(domain.getCalculationVersion())
                .evaluatedAt(domain.getEvaluatedAt())
                .build();
    }

    public static ImpactAssessment toDomain(ImpactAssessmentJpaEntity entity) {
        if (entity == null) return null;
        return ImpactAssessment.builder()
                .id(entity.getId())
                .assessmentNumber(entity.getAssessmentNumber())
                .title(entity.getTitle())
                .interventionType(entity.getInterventionType())
                .interventionId(entity.getInterventionId())
                .interventionReference(entity.getInterventionReference())
                .interventionDate(entity.getInterventionDate())
                .indicatorCode(entity.getIndicatorCode())
                .scopeType(entity.getScopeType())
                .scopeId(entity.getScopeId())
                .scopeName(entity.getScopeName())
                .baselineId(entity.getBaselineId())
                .baselinePeriodStart(entity.getBaselinePeriodStart())
                .baselinePeriodEnd(entity.getBaselinePeriodEnd())
                .baselineValue(entity.getBaselineValue())
                .evaluationPeriodStart(entity.getEvaluationPeriodStart())
                .evaluationPeriodEnd(entity.getEvaluationPeriodEnd())
                .postInterventionValue(entity.getPostInterventionValue())
                .absoluteChange(entity.getAbsoluteChange())
                .percentageChange(entity.getPercentageChange())
                .changeDirection(entity.getChangeDirection())
                .measurementUnit(entity.getMeasurementUnit())
                .evidenceClassification(entity.getEvidenceClassification())
                .baselineQuality(entity.getBaselineQuality())
                .attributionLevel(entity.getAttributionLevel())
                .confoundingFactors(entity.getConfoundingFactors())
                .dataLimitations(entity.getDataLimitations())
                .comparisonValid(entity.isComparisonValid())
                .methodologyNotes(entity.getMethodologyNotes())
                .reviewStatus(entity.getReviewStatus())
                .reviewerId(entity.getReviewerId())
                .reviewerName(entity.getReviewerName())
                .reviewNotes(entity.getReviewNotes())
                .reviewedAt(entity.getReviewedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ImpactAssessmentJpaEntity toEntity(ImpactAssessment domain) {
        if (domain == null) return null;
        return ImpactAssessmentJpaEntity.builder()
                .id(domain.getId())
                .assessmentNumber(domain.getAssessmentNumber())
                .title(domain.getTitle())
                .interventionType(domain.getInterventionType())
                .interventionId(domain.getInterventionId())
                .interventionReference(domain.getInterventionReference())
                .interventionDate(domain.getInterventionDate())
                .indicatorCode(domain.getIndicatorCode())
                .scopeType(domain.getScopeType())
                .scopeId(domain.getScopeId())
                .scopeName(domain.getScopeName())
                .baselineId(domain.getBaselineId())
                .baselinePeriodStart(domain.getBaselinePeriodStart())
                .baselinePeriodEnd(domain.getBaselinePeriodEnd())
                .baselineValue(domain.getBaselineValue())
                .evaluationPeriodStart(domain.getEvaluationPeriodStart())
                .evaluationPeriodEnd(domain.getEvaluationPeriodEnd())
                .postInterventionValue(domain.getPostInterventionValue())
                .absoluteChange(domain.getAbsoluteChange())
                .percentageChange(domain.getPercentageChange())
                .changeDirection(domain.getChangeDirection())
                .measurementUnit(domain.getMeasurementUnit())
                .evidenceClassification(domain.getEvidenceClassification())
                .baselineQuality(domain.getBaselineQuality())
                .attributionLevel(domain.getAttributionLevel())
                .confoundingFactors(domain.getConfoundingFactors())
                .dataLimitations(domain.getDataLimitations())
                .comparisonValid(domain.isComparisonValid())
                .methodologyNotes(domain.getMethodologyNotes())
                .reviewStatus(domain.getReviewStatus())
                .reviewerId(domain.getReviewerId())
                .reviewerName(domain.getReviewerName())
                .reviewNotes(domain.getReviewNotes())
                .reviewedAt(domain.getReviewedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
