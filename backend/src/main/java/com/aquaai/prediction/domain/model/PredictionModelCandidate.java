package com.aquaai.prediction.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable Domain Aggregate representing a governed Machine Learning model candidate.
 * Holds exact dataset snapshot linkage, feature schema version, reproducible artifact hash,
 * time-split metrics, and side-by-side baseline comparisons.
 */
@Getter
@Builder(toBuilder = true)
public class PredictionModelCandidate {

    private final UUID id;
    private final PredictionType predictionType;
    private final String modelType;
    private final String versionTag;
    private final UUID datasetSnapshotId;
    private final String featureSchemaVersion;
    private final Instant trainingPeriodStart;
    private final Instant trainingPeriodEnd;
    private final Instant validationPeriodStart;
    private final Instant validationPeriodEnd;
    private final Instant testPeriodStart;
    private final Instant testPeriodEnd;
    private final int trainingSampleCount;
    private final int validationSampleCount;
    private final int testSampleCount;
    private final String trainingMetricsJson;
    private final String validationMetricsJson;
    private final String testMetricsJson;
    private final String baselineComparisonMetricsJson;
    private final String configurationSnapshotJson;
    private final String artifactHash;
    private MethodVersionStatus status;
    private boolean isImmutable;
    private final Instant createdAt;
    private final String createdBy;
    private Long optLockVersion;

    public PredictionModelCandidate(
            UUID id,
            PredictionType predictionType,
            String modelType,
            String versionTag,
            UUID datasetSnapshotId,
            String featureSchemaVersion,
            Instant trainingPeriodStart,
            Instant trainingPeriodEnd,
            Instant validationPeriodStart,
            Instant validationPeriodEnd,
            Instant testPeriodStart,
            Instant testPeriodEnd,
            int trainingSampleCount,
            int validationSampleCount,
            int testSampleCount,
            String trainingMetricsJson,
            String validationMetricsJson,
            String testMetricsJson,
            String baselineComparisonMetricsJson,
            String configurationSnapshotJson,
            String artifactHash,
            MethodVersionStatus status,
            boolean isImmutable,
            Instant createdAt,
            String createdBy,
            Long optLockVersion
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.predictionType = Objects.requireNonNull(predictionType, "predictionType must not be null");
        this.modelType = Objects.requireNonNull(modelType, "modelType must not be null");
        this.versionTag = Objects.requireNonNull(versionTag, "versionTag must not be null");
        this.datasetSnapshotId = Objects.requireNonNull(datasetSnapshotId, "datasetSnapshotId must not be null");
        this.featureSchemaVersion = Objects.requireNonNull(featureSchemaVersion, "featureSchemaVersion must not be null");
        this.trainingPeriodStart = Objects.requireNonNull(trainingPeriodStart, "trainingPeriodStart must not be null");
        this.trainingPeriodEnd = Objects.requireNonNull(trainingPeriodEnd, "trainingPeriodEnd must not be null");
        this.validationPeriodStart = Objects.requireNonNull(validationPeriodStart, "validationPeriodStart must not be null");
        this.validationPeriodEnd = Objects.requireNonNull(validationPeriodEnd, "validationPeriodEnd must not be null");
        this.testPeriodStart = Objects.requireNonNull(testPeriodStart, "testPeriodStart must not be null");
        this.testPeriodEnd = Objects.requireNonNull(testPeriodEnd, "testPeriodEnd must not be null");
        this.trainingSampleCount = trainingSampleCount;
        this.validationSampleCount = validationSampleCount;
        this.testSampleCount = testSampleCount;
        this.trainingMetricsJson = Objects.requireNonNullElse(trainingMetricsJson, "{}");
        this.validationMetricsJson = Objects.requireNonNullElse(validationMetricsJson, "{}");
        this.testMetricsJson = Objects.requireNonNullElse(testMetricsJson, "{}");
        this.baselineComparisonMetricsJson = Objects.requireNonNullElse(baselineComparisonMetricsJson, "{}");
        this.configurationSnapshotJson = Objects.requireNonNullElse(configurationSnapshotJson, "{}");
        this.artifactHash = Objects.requireNonNull(artifactHash, "artifactHash must not be null");
        this.status = status != null ? status : MethodVersionStatus.CANDIDATE;
        this.isImmutable = isImmutable;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.createdBy = createdBy != null ? createdBy : "SYSTEM";
        this.optLockVersion = optLockVersion != null ? optLockVersion : 0L;

        // Hard safety parameter validation
        PredictionMethodVersion.validateSafetyParameters(this.configurationSnapshotJson);
    }

    public void approve(String actor) {
        this.status = MethodVersionStatus.APPROVED;
    }

    public void setShadow(String actor) {
        this.status = MethodVersionStatus.SHADOW;
        this.isImmutable = true;
    }

    public void setCanary(String actor) {
        this.status = MethodVersionStatus.CANARY;
        this.isImmutable = true;
    }

    public void activate(String actor) {
        this.status = MethodVersionStatus.ACTIVE;
        this.isImmutable = true;
    }

    public void reject(String actor) {
        this.status = MethodVersionStatus.RETIRED;
        this.isImmutable = true;
    }

    public void retire(String actor) {
        this.status = MethodVersionStatus.RETIRED;
        this.isImmutable = true;
    }

    public void rollback(String actor) {
        this.status = MethodVersionStatus.RETIRED;
        this.isImmutable = true;
    }
}
