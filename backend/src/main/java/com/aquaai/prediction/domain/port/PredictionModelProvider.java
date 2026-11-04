package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;

import java.util.Optional;

/**
 * Clean model provider abstraction for pluggable forecasting engines.
 * Allows deterministic baseline providers, JVM-native ML regression providers,
 * and future ML candidates to participate in hybrid prediction without framework lock-in.
 */
public interface PredictionModelProvider {

    String getProviderName();

    String getModelType();

    boolean supports(PredictionType predictionType);

    /**
     * Computes a candidate model prediction or residual correction for soil moisture.
     * Returns Optional.empty() if candidate fails, inputs are out of bounds, or data is insufficient,
     * immediately triggering safe deterministic baseline fallback.
     */
    Optional<Double> predictSoilMoisture(
            PredictionDataProvider.FieldMetadata field,
            PredictionHorizon horizon,
            double currentMoisture,
            double slope24h,
            PredictionDataProvider.WeatherContext weather,
            double recentIrrigationVolume7d,
            double waterHeadroomPercent,
            double telemetryQualityScore,
            String configurationJson
    );
}
