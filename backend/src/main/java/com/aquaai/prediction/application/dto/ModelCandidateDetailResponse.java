package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ModelCandidateDetailResponse(
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
        Map<String, Double> featureImportance,
        String limitations
) {}
