package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.PredictionType;

import java.util.UUID;

public record TrainPredictionModelResponse(
        String status, // SUCCESS, INSUFFICIENT_TRAINING_DATA, FAILED
        String message,
        PredictionType predictionType,
        UUID datasetId,
        String datasetHash,
        int sampleCount,
        int minRequiredSamples,
        UUID candidateId,
        String versionTag,
        String baselineComparisonSummary,
        Double baselineMae,
        Double candidateMae,
        Double improvementPercent,
        boolean regressionDetected
) {
    public static TrainPredictionModelResponse insufficientData(
            PredictionType type,
            UUID datasetId,
            String datasetHash,
            int sampleCount,
            int required,
            String reason
    ) {
        return new TrainPredictionModelResponse(
                "INSUFFICIENT_TRAINING_DATA",
                reason,
                type,
                datasetId,
                datasetHash,
                sampleCount,
                required,
                null,
                null,
                "Deterministic baseline remains authoritative. Insufficient verified training data.",
                null,
                null,
                0.0,
                false
        );
    }
}
