package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.PredictionClassificationResult;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import org.springframework.stereotype.Component;

@Component
public class PredictionEvaluationJpaMapper {

    public PredictionEvaluationJpaEntity toEntity(PredictionEvaluation domain) {
        if (domain == null) return null;
        return PredictionEvaluationJpaEntity.builder()
                .id(domain.getId())
                .predictionId(domain.getPredictionId())
                .fieldId(domain.getFieldId())
                .predictionType(domain.getPredictionType().name())
                .horizon(domain.getHorizon().name())
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
                .classificationResult(domain.getClassificationResult() != null ? domain.getClassificationResult().name() : "NOT_APPLICABLE")
                .recommendedValue(domain.getRecommendedValue())
                .approvedValue(domain.getApprovedValue())
                .operationalEventId(domain.getOperationalEventId())
                .operationalNotes(domain.getOperationalNotes())
                .safetyInterlockTriggered(domain.isSafetyInterlockTriggered())
                .confidencePercent(domain.getConfidencePercent())
                .evaluationStatus(domain.getEvaluationStatus().name())
                .failureReason(domain.getFailureReason().name())
                .dataQuality(domain.getDataQuality().name())
                .evaluationMethod(domain.getEvaluationMethod())
                .notes(domain.getNotes())
                .build();
    }

    public PredictionEvaluation toDomain(PredictionEvaluationJpaEntity entity) {
        if (entity == null) return null;
        return PredictionEvaluation.builder()
                .id(entity.getId())
                .predictionId(entity.getPredictionId())
                .fieldId(entity.getFieldId())
                .predictionType(PredictionType.valueOf(entity.getPredictionType()))
                .horizon(PredictionHorizon.valueOf(entity.getHorizon()))
                .modelVersion(entity.getModelVersion())
                .generatedAt(entity.getGeneratedAt())
                .targetTime(entity.getTargetTime())
                .evaluatedAt(entity.getEvaluatedAt())
                .actualObservedAt(entity.getActualObservedAt())
                .predictedValue(entity.getPredictedValue())
                .lowerBound(entity.getLowerBound())
                .upperBound(entity.getUpperBound())
                .actualValue(entity.getActualValue())
                .absoluteError(entity.getAbsoluteError())
                .relativeErrorPercent(entity.getRelativeErrorPercent())
                .directionalError(entity.getDirectionalError())
                .isWithinBounds(entity.getIsWithinBounds())
                .predictedClass(entity.getPredictedClass())
                .actualClass(entity.getActualClass())
                .classificationResult(entity.getClassificationResult() != null
                        ? PredictionClassificationResult.valueOf(entity.getClassificationResult())
                        : PredictionClassificationResult.NOT_APPLICABLE)
                .recommendedValue(entity.getRecommendedValue())
                .approvedValue(entity.getApprovedValue())
                .operationalEventId(entity.getOperationalEventId())
                .operationalNotes(entity.getOperationalNotes())
                .safetyInterlockTriggered(entity.isSafetyInterlockTriggered())
                .confidencePercent(entity.getConfidencePercent())
                .evaluationStatus(EvaluationStatus.valueOf(entity.getEvaluationStatus()))
                .failureReason(EvaluationFailureReason.valueOf(entity.getFailureReason()))
                .dataQuality(EvaluationDataQuality.valueOf(entity.getDataQuality()))
                .evaluationMethod(entity.getEvaluationMethod())
                .notes(entity.getNotes())
                .build();
    }
}
