package com.aquaai.prediction.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable Dataset Snapshot representing a verified, reproducible historical training corpus.
 * Every trained ML model candidate is permanently traceable to the specific dataset hash and snapshot ID.
 */
@Getter
@Builder(toBuilder = true)
public class PredictionTrainingDataset {

    private final UUID id;
    private final PredictionType predictionType;
    private final String datasetHash;
    private final String featureSchemaVersion;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final String fieldScope;
    private final int recordCount;
    private final TrainingDatasetQuality.DataQualityStatus qualityStatus;
    private final String qualitySummaryJson;
    private final Instant createdAt;
    private final String createdBy;

    public PredictionTrainingDataset(
            UUID id,
            PredictionType predictionType,
            String datasetHash,
            String featureSchemaVersion,
            Instant periodStart,
            Instant periodEnd,
            String fieldScope,
            int recordCount,
            TrainingDatasetQuality.DataQualityStatus qualityStatus,
            String qualitySummaryJson,
            Instant createdAt,
            String createdBy
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.predictionType = Objects.requireNonNull(predictionType, "predictionType must not be null");
        this.datasetHash = Objects.requireNonNull(datasetHash, "datasetHash must not be null");
        this.featureSchemaVersion = Objects.requireNonNull(featureSchemaVersion, "featureSchemaVersion must not be null");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart must not be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd must not be null");
        this.fieldScope = fieldScope != null ? fieldScope : "ALL";
        this.recordCount = recordCount;
        this.qualityStatus = Objects.requireNonNull(qualityStatus, "qualityStatus must not be null");
        this.qualitySummaryJson = Objects.requireNonNullElse(qualitySummaryJson, "{}");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.createdBy = createdBy != null ? createdBy : "SYSTEM";
    }

    public static PredictionTrainingDataset create(
            PredictionType predictionType,
            String datasetHash,
            String featureSchemaVersion,
            Instant periodStart,
            Instant periodEnd,
            String fieldScope,
            int recordCount,
            TrainingDatasetQuality quality,
            String qualitySummaryJson,
            String createdBy
    ) {
        return new PredictionTrainingDataset(
                UUID.randomUUID(),
                predictionType,
                datasetHash,
                featureSchemaVersion,
                periodStart,
                periodEnd,
                fieldScope,
                recordCount,
                quality != null ? quality.qualityStatus() : TrainingDatasetQuality.DataQualityStatus.INSUFFICIENT,
                qualitySummaryJson,
                Instant.now(),
                createdBy
        );
    }
}
