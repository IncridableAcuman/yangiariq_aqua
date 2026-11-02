package com.aquaai.prediction.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable Domain Aggregate representing the evaluated outcome and accuracy metrics
 * of an individual forecast.
 */
@Getter
@Builder(toBuilder = true)
public class PredictionEvaluation {

    private final UUID id;
    private final UUID predictionId;
    private final UUID fieldId;
    private final PredictionType predictionType;
    private final PredictionHorizon horizon;
    private final String modelVersion;
    private final Instant generatedAt;
    private final Instant targetTime;
    private final Instant evaluatedAt;
    private final Instant actualObservedAt;

    // Numerical evaluation
    private final BigDecimal predictedValue;
    private final BigDecimal lowerBound;
    private final BigDecimal upperBound;
    private final BigDecimal actualValue;
    private final BigDecimal absoluteError;
    private final BigDecimal relativeErrorPercent;
    private final BigDecimal directionalError;
    private final Boolean isWithinBounds;

    // Classification evaluation
    private final String predictedClass;
    private final String actualClass;
    private final PredictionClassificationResult classificationResult;

    // Operational Context & Human Decision Tracking
    private final BigDecimal recommendedValue;
    private final BigDecimal approvedValue;
    private final String operationalEventId;
    private final String operationalNotes;
    private final boolean safetyInterlockTriggered;

    // Evaluation Status & Data Quality
    private final int confidencePercent;
    private final EvaluationStatus evaluationStatus;
    private final EvaluationFailureReason failureReason;
    private final EvaluationDataQuality dataQuality;
    private final String evaluationMethod;
    private final String notes;

    public static PredictionEvaluation createNumerical(
            UUID predictionId,
            UUID fieldId,
            PredictionType predictionType,
            PredictionHorizon horizon,
            String modelVersion,
            Instant generatedAt,
            Instant targetTime,
            Instant actualObservedAt,
            BigDecimal predictedValue,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            BigDecimal actualValue,
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
        BigDecimal absError = null;
        BigDecimal relError = null;
        BigDecimal dirError = null;
        Boolean withinBounds = null;

        if (predictedValue != null && actualValue != null) {
            absError = predictedValue.subtract(actualValue).abs().setScale(4, RoundingMode.HALF_UP);
            dirError = predictedValue.subtract(actualValue).setScale(4, RoundingMode.HALF_UP);
            if (actualValue.compareTo(BigDecimal.ZERO) != 0) {
                relError = absError.divide(actualValue.abs(), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(4, RoundingMode.HALF_UP);
            }

            if (lowerBound != null && upperBound != null) {
                withinBounds = actualValue.compareTo(lowerBound) >= 0 && actualValue.compareTo(upperBound) <= 0;
            }
        }

        return PredictionEvaluation.builder()
                .id(UUID.randomUUID())
                .predictionId(Objects.requireNonNull(predictionId, "predictionId must not be null"))
                .fieldId(Objects.requireNonNull(fieldId, "fieldId must not be null"))
                .predictionType(Objects.requireNonNull(predictionType, "predictionType must not be null"))
                .horizon(Objects.requireNonNull(horizon, "horizon must not be null"))
                .modelVersion(modelVersion != null ? modelVersion : "DETERMINISTIC_V1")
                .generatedAt(Objects.requireNonNull(generatedAt, "generatedAt must not be null"))
                .targetTime(Objects.requireNonNull(targetTime, "targetTime must not be null"))
                .evaluatedAt(Instant.now())
                .actualObservedAt(actualObservedAt)
                .predictedValue(predictedValue)
                .lowerBound(lowerBound)
                .upperBound(upperBound)
                .actualValue(actualValue)
                .absoluteError(absError)
                .relativeErrorPercent(relError)
                .directionalError(dirError)
                .isWithinBounds(withinBounds)
                .predictedClass(null)
                .actualClass(null)
                .classificationResult(PredictionClassificationResult.NOT_APPLICABLE)
                .recommendedValue(recommendedValue)
                .approvedValue(approvedValue)
                .operationalEventId(operationalEventId)
                .operationalNotes(operationalNotes)
                .safetyInterlockTriggered(safetyInterlockTriggered)
                .confidencePercent(confidencePercent)
                .evaluationStatus(evaluationStatus != null ? evaluationStatus : EvaluationStatus.EVALUATED)
                .failureReason(failureReason != null ? failureReason : EvaluationFailureReason.NONE)
                .dataQuality(dataQuality != null ? dataQuality : EvaluationDataQuality.DATA_COMPLETE)
                .evaluationMethod(evaluationMethod != null ? evaluationMethod : "NUMERICAL_DIFFERENCE")
                .notes(notes != null ? notes : "")
                .build();
    }

    public static PredictionEvaluation createCategorical(
            UUID predictionId,
            UUID fieldId,
            PredictionType predictionType,
            PredictionHorizon horizon,
            String modelVersion,
            Instant generatedAt,
            Instant targetTime,
            Instant actualObservedAt,
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
        return PredictionEvaluation.builder()
                .id(UUID.randomUUID())
                .predictionId(Objects.requireNonNull(predictionId, "predictionId must not be null"))
                .fieldId(Objects.requireNonNull(fieldId, "fieldId must not be null"))
                .predictionType(Objects.requireNonNull(predictionType, "predictionType must not be null"))
                .horizon(Objects.requireNonNull(horizon, "horizon must not be null"))
                .modelVersion(modelVersion != null ? modelVersion : "DETERMINISTIC_V1")
                .generatedAt(Objects.requireNonNull(generatedAt, "generatedAt must not be null"))
                .targetTime(Objects.requireNonNull(targetTime, "targetTime must not be null"))
                .evaluatedAt(Instant.now())
                .actualObservedAt(actualObservedAt)
                .predictedValue(null)
                .lowerBound(null)
                .upperBound(null)
                .actualValue(null)
                .absoluteError(null)
                .relativeErrorPercent(null)
                .directionalError(null)
                .isWithinBounds(null)
                .predictedClass(predictedClass)
                .actualClass(actualClass)
                .classificationResult(classificationResult != null ? classificationResult : PredictionClassificationResult.NOT_APPLICABLE)
                .recommendedValue(recommendedValue)
                .approvedValue(approvedValue)
                .operationalEventId(operationalEventId)
                .operationalNotes(operationalNotes)
                .safetyInterlockTriggered(safetyInterlockTriggered)
                .confidencePercent(confidencePercent)
                .evaluationStatus(evaluationStatus != null ? evaluationStatus : EvaluationStatus.EVALUATED)
                .failureReason(failureReason != null ? failureReason : EvaluationFailureReason.NONE)
                .dataQuality(dataQuality != null ? dataQuality : EvaluationDataQuality.DATA_COMPLETE)
                .evaluationMethod(evaluationMethod != null ? evaluationMethod : "CONFUSION_MATRIX_MATCHING")
                .notes(notes != null ? notes : "")
                .build();
    }

    public static PredictionEvaluation createInsufficientData(
            UUID predictionId,
            UUID fieldId,
            PredictionType predictionType,
            PredictionHorizon horizon,
            String modelVersion,
            Instant generatedAt,
            Instant targetTime,
            int confidencePercent,
            EvaluationFailureReason reason,
            String notes
    ) {
        return PredictionEvaluation.builder()
                .id(UUID.randomUUID())
                .predictionId(predictionId)
                .fieldId(fieldId)
                .predictionType(predictionType)
                .horizon(horizon)
                .modelVersion(modelVersion != null ? modelVersion : "DETERMINISTIC_V1")
                .generatedAt(generatedAt)
                .targetTime(targetTime)
                .evaluatedAt(Instant.now())
                .actualObservedAt(null)
                .predictedValue(null)
                .lowerBound(null)
                .upperBound(null)
                .actualValue(null)
                .absoluteError(null)
                .relativeErrorPercent(null)
                .directionalError(null)
                .isWithinBounds(null)
                .predictedClass(null)
                .actualClass(null)
                .classificationResult(PredictionClassificationResult.NOT_APPLICABLE)
                .recommendedValue(null)
                .approvedValue(null)
                .operationalEventId(null)
                .operationalNotes(null)
                .safetyInterlockTriggered(false)
                .confidencePercent(confidencePercent)
                .evaluationStatus(EvaluationStatus.INSUFFICIENT_DATA)
                .failureReason(reason != null ? reason : EvaluationFailureReason.INSUFFICIENT_DATA)
                .dataQuality(EvaluationDataQuality.DATA_INSUFFICIENT)
                .evaluationMethod("NO_OUTCOME_OBSERVATION")
                .notes(notes != null ? notes : "Outcome observations unavailable within evaluation window.")
                .build();
    }
}
