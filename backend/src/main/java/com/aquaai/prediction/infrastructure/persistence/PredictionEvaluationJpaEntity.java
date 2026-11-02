package com.aquaai.prediction.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prediction_evaluations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionEvaluationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "prediction_id", nullable = false, unique = true)
    private UUID predictionId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "horizon", nullable = false, length = 32)
    private String horizon;

    @Column(name = "model_version", nullable = false, length = 64)
    private String modelVersion;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "target_time", nullable = false)
    private Instant targetTime;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Column(name = "actual_observed_at")
    private Instant actualObservedAt;

    @Column(name = "predicted_value", precision = 12, scale = 2)
    private BigDecimal predictedValue;

    @Column(name = "lower_bound", precision = 12, scale = 2)
    private BigDecimal lowerBound;

    @Column(name = "upper_bound", precision = 12, scale = 2)
    private BigDecimal upperBound;

    @Column(name = "actual_value", precision = 12, scale = 2)
    private BigDecimal actualValue;

    @Column(name = "absolute_error", precision = 12, scale = 4)
    private BigDecimal absoluteError;

    @Column(name = "relative_error_percent", precision = 8, scale = 4)
    private BigDecimal relativeErrorPercent;

    @Column(name = "directional_error", precision = 12, scale = 4)
    private BigDecimal directionalError;

    @Column(name = "is_within_bounds")
    private Boolean isWithinBounds;

    @Column(name = "predicted_class", length = 64)
    private String predictedClass;

    @Column(name = "actual_class", length = 64)
    private String actualClass;

    @Column(name = "classification_result", nullable = false, length = 32)
    private String classificationResult;

    @Column(name = "recommended_value", precision = 12, scale = 2)
    private BigDecimal recommendedValue;

    @Column(name = "approved_value", precision = 12, scale = 2)
    private BigDecimal approvedValue;

    @Column(name = "operational_event_id", length = 64)
    private String operationalEventId;

    @Column(name = "operational_notes", columnDefinition = "TEXT")
    private String operationalNotes;

    @Column(name = "safety_interlock_triggered", nullable = false)
    @Builder.Default
    private boolean safetyInterlockTriggered = false;

    @Column(name = "confidence_percent", nullable = false)
    private int confidencePercent;

    @Column(name = "evaluation_status", nullable = false, length = 32)
    private String evaluationStatus;

    @Column(name = "failure_reason", nullable = false, length = 64)
    private String failureReason;

    @Column(name = "data_quality", nullable = false, length = 32)
    private String dataQuality;

    @Column(name = "evaluation_method", nullable = false, length = 64)
    private String evaluationMethod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
