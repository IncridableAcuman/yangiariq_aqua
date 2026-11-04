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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prediction_training_datasets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionTrainingDatasetJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "dataset_hash", nullable = false, length = 128)
    private String datasetHash;

    @Column(name = "feature_schema_version", nullable = false, length = 64)
    private String featureSchemaVersion;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "field_scope", nullable = false, length = 64)
    private String fieldScope;

    @Column(name = "record_count", nullable = false)
    private int recordCount;

    @Column(name = "quality_status", nullable = false, length = 32)
    private String qualityStatus;

    @Column(name = "quality_summary_json", columnDefinition = "TEXT", nullable = false)
    private String qualitySummaryJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;
}
