package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;

import java.util.List;
import java.util.UUID;

/**
 * Model provider abstraction for generating predictions.
 * Allows decoupling the domain from specific forecasting engines
 * (e.g., deterministic/statistical vs future ML models).
 */
public interface PredictionProvider {

    String getProviderName();

    List<Prediction> generateFieldPredictions(
            PredictionDataProvider.FieldMetadata field,
            PredictionHorizon horizon,
            PredictionDataProvider dataProvider
    );

    Prediction generateSpecificPrediction(
            PredictionDataProvider.FieldMetadata field,
            PredictionType type,
            PredictionHorizon horizon,
            PredictionDataProvider dataProvider
    );
}
