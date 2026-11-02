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
@Table(name = "predictions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "horizon", nullable = false, length = 32)
    private String horizon;

    @Column(name = "target_time", nullable = false)
    private Instant targetTime;

    @Column(name = "predicted_value", precision = 12, scale = 2)
    private BigDecimal predictedValue;

    @Column(name = "lower_bound", precision = 12, scale = 2)
    private BigDecimal lowerBound;

    @Column(name = "upper_bound", precision = 12, scale = 2)
    private BigDecimal upperBound;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "confidence_percent", nullable = false)
    private int confidencePercent;

    @Column(name = "risk_level", nullable = false, length = 32)
    private String riskLevel;

    @Column(name = "data_sufficiency", nullable = false, length = 32)
    private String dataSufficiency;

    @Column(name = "likelihood", length = 32)
    private String likelihood;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "assumptions", columnDefinition = "TEXT")
    private String assumptions;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "action_target_module", length = 64)
    private String actionTargetModule;

    @Column(name = "actual_outcome_value", precision = 12, scale = 2)
    private BigDecimal actualOutcomeValue;

    @Column(name = "outcome_recorded_at")
    private Instant outcomeRecordedAt;

    @Column(name = "accuracy_status", nullable = false, length = 32)
    private String accuracyStatus;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
}
