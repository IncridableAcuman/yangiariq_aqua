package com.aquaai.pilot.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pilot_prediction_validations")
public class PredictionValidationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "pilot_id", nullable = false)
    private UUID pilotId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "prediction_id", length = 128)
    private String predictionId;

    @Column(name = "model_name", nullable = false, length = 128)
    private String modelName;

    @Column(name = "model_version", nullable = false, length = 64)
    private String modelVersion;

    @Column(name = "predicted_at", nullable = false)
    private Instant predictedAt;

    @Column(name = "prediction_horizon_hours", nullable = false)
    private int predictionHorizonHours;

    @Column(name = "predicted_value", nullable = false)
    private Double predictedValue;

    @Column(name = "actual_value", nullable = false)
    private Double actualValue;

    @Column(name = "ground_truth_id")
    private UUID groundTruthId;

    @Column(name = "absolute_error", nullable = false)
    private Double absoluteError;

    @Column(name = "relative_error_percentage")
    private Double relativeErrorPercentage;

    @Column(name = "evaluation_status", nullable = false, length = 32)
    private String evaluationStatus;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    public PredictionValidationEntity() {}

    public PredictionValidationEntity(
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
            String evaluationStatus,
            Instant evaluatedAt) {
        this.id = id;
        this.pilotId = pilotId;
        this.fieldId = fieldId;
        this.predictionId = predictionId;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.predictedAt = predictedAt;
        this.predictionHorizonHours = predictionHorizonHours;
        this.predictedValue = predictedValue;
        this.actualValue = actualValue;
        this.groundTruthId = groundTruthId;
        this.absoluteError = absoluteError;
        this.relativeErrorPercentage = relativeErrorPercentage;
        this.evaluationStatus = evaluationStatus;
        this.evaluatedAt = evaluatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPilotId() { return pilotId; }
    public void setPilotId(UUID pilotId) { this.pilotId = pilotId; }
    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }
    public String getPredictionId() { return predictionId; }
    public void setPredictionId(String predictionId) { this.predictionId = predictionId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public Instant getPredictedAt() { return predictedAt; }
    public void setPredictedAt(Instant predictedAt) { this.predictedAt = predictedAt; }
    public int getPredictionHorizonHours() { return predictionHorizonHours; }
    public void setPredictionHorizonHours(int predictionHorizonHours) { this.predictionHorizonHours = predictionHorizonHours; }
    public Double getPredictedValue() { return predictedValue; }
    public void setPredictedValue(Double predictedValue) { this.predictedValue = predictedValue; }
    public Double getActualValue() { return actualValue; }
    public void setActualValue(Double actualValue) { this.actualValue = actualValue; }
    public UUID getGroundTruthId() { return groundTruthId; }
    public void setGroundTruthId(UUID groundTruthId) { this.groundTruthId = groundTruthId; }
    public Double getAbsoluteError() { return absoluteError; }
    public void setAbsoluteError(Double absoluteError) { this.absoluteError = absoluteError; }
    public Double getRelativeErrorPercentage() { return relativeErrorPercentage; }
    public void setRelativeErrorPercentage(Double relativeErrorPercentage) { this.relativeErrorPercentage = relativeErrorPercentage; }
    public String getEvaluationStatus() { return evaluationStatus; }
    public void setEvaluationStatus(String evaluationStatus) { this.evaluationStatus = evaluationStatus; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
