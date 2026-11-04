package com.aquaai.prediction.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prediction_model_candidates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionModelCandidateJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "model_type", nullable = false, length = 64)
    private String modelType;

    @Column(name = "version_tag", nullable = false, unique = true, length = 64)
    private String versionTag;

    @Column(name = "dataset_snapshot_id", nullable = false)
    private UUID datasetSnapshotId;

    @Column(name = "feature_schema_version", nullable = false, length = 64)
    private String featureSchemaVersion;

    @Column(name = "training_period_start", nullable = false)
    private Instant trainingPeriodStart;

    @Column(name = "training_period_end", nullable = false)
    private Instant trainingPeriodEnd;

    @Column(name = "validation_period_start", nullable = false)
    private Instant validationPeriodStart;

    @Column(name = "validation_period_end", nullable = false)
    private Instant validationPeriodEnd;

    @Column(name = "test_period_start", nullable = false)
    private Instant testPeriodStart;

    @Column(name = "test_period_end", nullable = false)
    private Instant testPeriodEnd;

    @Column(name = "training_sample_count", nullable = false)
    private int trainingSampleCount;

    @Column(name = "validation_sample_count", nullable = false)
    private int validationSampleCount;

    @Column(name = "test_sample_count", nullable = false)
    private int testSampleCount;

    @Column(name = "training_metrics_json", columnDefinition = "TEXT", nullable = false)
    private String trainingMetricsJson;

    @Column(name = "validation_metrics_json", columnDefinition = "TEXT", nullable = false)
    private String validationMetricsJson;

    @Column(name = "test_metrics_json", columnDefinition = "TEXT", nullable = false)
    private String testMetricsJson;

    @Column(name = "baseline_comparison_metrics_json", columnDefinition = "TEXT", nullable = false)
    private String baselineComparisonMetricsJson;

    @Column(name = "configuration_snapshot_json", columnDefinition = "TEXT", nullable = false)
    private String configurationSnapshotJson;

    @Column(name = "artifact_hash", nullable = false, length = 128)
    private String artifactHash;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "is_immutable", nullable = false)
    private boolean isImmutable;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Version
    @Column(name = "opt_lock_version", nullable = false)
    private Long optLockVersion;
}
