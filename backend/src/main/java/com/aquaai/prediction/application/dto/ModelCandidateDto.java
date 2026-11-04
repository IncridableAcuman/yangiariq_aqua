package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionType;

import java.time.Instant;
import java.util.UUID;

public record ModelCandidateDto(
        UUID id,
        PredictionType predictionType,
        String modelType,
        String versionTag,
        UUID datasetSnapshotId,
        String featureSchemaVersion,
        int trainingSampleCount,
        int validationSampleCount,
        int testSampleCount,
        String artifactHash,
        MethodVersionStatus status,
        boolean isImmutable,
        Instant createdAt,
        String createdBy,
        Double testMae,
        Double baselineMae,
        Double maeImprovementPercent,
        boolean regressionDetected,
        String recommendationStatus
) {}
