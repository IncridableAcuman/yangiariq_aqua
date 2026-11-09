package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PredictionValidationRecord {
    private final UUID id;
    private final UUID pilotId;
    private final UUID fieldId;
    private final String predictionId;
    private final String modelName;
    private final String modelVersion;
    private final Instant predictedAt;
    private final int predictionHorizonHours;
    private final Double predictedValue;
    private final Double actualValue;
    private final UUID groundTruthId;
    private final Double absoluteError;
    private final Double relativeErrorPercentage;
    private final PredictionEvaluationStatus evaluationStatus;
    private final Instant evaluatedAt;

    public PredictionValidationRecord(
            UUID id,
            UUID pilotId,
            UUID fieldId,
            String predictionId,
            String modelName,
            String modelVersion,
            Instant predictedAt,
            int predictionHorizonHours,
            Double predictedValue,
            Double actualValue,
            UUID groundTruthId,
            Double absoluteError,
            Double relativeErrorPercentage,
            PredictionEvaluationStatus evaluationStatus,
            Instant evaluatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.pilotId = Objects.requireNonNull(pilotId, "pilotId must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.predictionId = predictionId;
        this.modelName = Objects.requireNonNull(modelName, "modelName must not be null");
        this.modelVersion = Objects.requireNonNull(modelVersion, "modelVersion must not be null");
        this.predictedAt = Objects.requireNonNull(predictedAt, "predictedAt must not be null");
        this.predictionHorizonHours = predictionHorizonHours;
        this.predictedValue = Objects.requireNonNull(predictedValue, "predictedValue must not be null");
        this.actualValue = Objects.requireNonNull(actualValue, "actualValue must not be null");
        this.groundTruthId = groundTruthId;
        this.absoluteError = absoluteError != null ? absoluteError : Math.abs(predictedValue - actualValue);
        this.relativeErrorPercentage = relativeErrorPercentage != null ? relativeErrorPercentage :
                (Math.abs(actualValue) > 0.0001 ? (Math.abs(predictedValue - actualValue) / Math.abs(actualValue)) * 100.0 : null);
        this.evaluationStatus = evaluationStatus != null ? evaluationStatus : PredictionEvaluationStatus.VALIDATED;
        this.evaluatedAt = evaluatedAt != null ? evaluatedAt : Instant.now();
    }

    public static PredictionValidationRecord create(
            UUID pilotId,
            UUID fieldId,
            String predictionId,
            String modelName,
            String modelVersion,
            Instant predictedAt,
            int predictionHorizonHours,
            Double predictedValue,
            Double actualValue,
            UUID groundTruthId) {
        double rawAbs = Math.abs(predictedValue - actualValue);
        double absError = Math.round(rawAbs * 10000.0) / 10000.0;
        Double relError = Math.abs(actualValue) > 0.0001 ? Math.round(((absError / Math.abs(actualValue)) * 100.0) * 10000.0) / 10000.0 : null;
        return new PredictionValidationRecord(
                UUID.randomUUID(),
                pilotId,
                fieldId,
                predictionId,
                modelName,
                modelVersion,
                predictedAt,
                predictionHorizonHours,
                predictedValue,
                actualValue,
                groundTruthId,
                absError,
                relError,
                PredictionEvaluationStatus.VALIDATED,
                Instant.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getPilotId() { return pilotId; }
    public UUID getFieldId() { return fieldId; }
    public String getPredictionId() { return predictionId; }
    public String getModelName() { return modelName; }
    public String getModelVersion() { return modelVersion; }
    public Instant getPredictedAt() { return predictedAt; }
    public int getPredictionHorizonHours() { return predictionHorizonHours; }
    public Double getPredictedValue() { return predictedValue; }
    public Double getActualValue() { return actualValue; }
    public UUID getGroundTruthId() { return groundTruthId; }
    public Double getAbsoluteError() { return absoluteError; }
    public Double getRelativeErrorPercentage() { return relativeErrorPercentage; }
    public PredictionEvaluationStatus getEvaluationStatus() { return evaluationStatus; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
}
