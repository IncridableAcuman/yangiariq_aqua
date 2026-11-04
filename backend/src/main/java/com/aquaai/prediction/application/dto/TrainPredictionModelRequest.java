package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.PredictionType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record TrainPredictionModelRequest(
        @NotNull PredictionType predictionType,
        UUID fieldId,
        Instant periodStart,
        Instant periodEnd,
        Double l2Penalty
) {
    public TrainPredictionModelRequest {
        predictionType = predictionType != null ? predictionType : PredictionType.SOIL_MOISTURE_FORECAST;
    }
}
