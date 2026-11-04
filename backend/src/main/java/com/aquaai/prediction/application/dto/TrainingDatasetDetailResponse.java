package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.TrainingDatasetQuality;

import java.time.Instant;
import java.util.UUID;

public record TrainingDatasetDetailResponse(
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
) {}
