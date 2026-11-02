package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.PredictionClassificationResult;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record PredictionEvaluationDetailDto(
        UUID id,
        UUID predictionId,
        UUID fieldId,
        String fieldName,
        PredictionType predictionType,
        PredictionHorizon horizon,
        String modelVersion,
        Instant generatedAt,
        Instant targetTime,
        Instant evaluatedAt,
        Instant actualObservedAt,
        BigDecimal predictedValue,
        BigDecimal lowerBound,
        BigDecimal upperBound,
        BigDecimal actualValue,
        BigDecimal absoluteError,
        BigDecimal relativeErrorPercent,
        BigDecimal directionalError,
        Boolean isWithinBounds,
        String predictedClass,
        String actualClass,
        PredictionClassificationResult classificationResult,
        BigDecimal recommendedValue,
        BigDecimal approvedValue,
        String operationalEventId,
        String operationalNotes,
        boolean safetyInterlockTriggered,
        int confidencePercent,
        EvaluationStatus evaluationStatus,
        EvaluationFailureReason failureReason,
        EvaluationDataQuality dataQuality,
        String evaluationMethod,
        String notes
) {
    public static PredictionEvaluationDetailDto fromDomain(PredictionEvaluation domain, String fieldName) {
        if (domain == null) return null;
        return PredictionEvaluationDetailDto.builder()
                .id(domain.getId())
                .predictionId(domain.getPredictionId())
                .fieldId(domain.getFieldId())
                .fieldName(fieldName != null ? fieldName : "Field " + domain.getFieldId().toString().substring(0, 8))
                .predictionType(domain.getPredictionType())
                .horizon(domain.getHorizon())
                .modelVersion(domain.getModelVersion())
                .generatedAt(domain.getGeneratedAt())
                .targetTime(domain.getTargetTime())
                .evaluatedAt(domain.getEvaluatedAt())
                .actualObservedAt(domain.getActualObservedAt())
                .predictedValue(domain.getPredictedValue())
                .lowerBound(domain.getLowerBound())
                .upperBound(domain.getUpperBound())
                .actualValue(domain.getActualValue())
                .absoluteError(domain.getAbsoluteError())
                .relativeErrorPercent(domain.getRelativeErrorPercent())
                .directionalError(domain.getDirectionalError())
                .isWithinBounds(domain.getIsWithinBounds())
                .predictedClass(domain.getPredictedClass())
                .actualClass(domain.getActualClass())
                .classificationResult(domain.getClassificationResult())
                .recommendedValue(domain.getRecommendedValue())
                .approvedValue(domain.getApprovedValue())
                .operationalEventId(domain.getOperationalEventId())
                .operationalNotes(domain.getOperationalNotes())
                .safetyInterlockTriggered(domain.isSafetyInterlockTriggered())
                .confidencePercent(domain.getConfidencePercent())
                .evaluationStatus(domain.getEvaluationStatus())
                .failureReason(domain.getFailureReason())
                .dataQuality(domain.getDataQuality())
                .evaluationMethod(domain.getEvaluationMethod())
                .notes(domain.getNotes())
                .build();
    }
}
