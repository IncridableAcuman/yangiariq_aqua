package com.aquaai.risk.domain.port;

import com.aquaai.prediction.domain.model.ModelHealthStatus;

import java.util.UUID;

public interface RiskPredictionPort {

    Double getPredictedSoilMoistureNext24h(UUID fieldId);

    Double getPredictedWaterDemandNext7d(UUID fieldId);

    ModelHealthStatus getPredictionModelHealth();

    boolean isPredictionModelDriftDetected();

    int getPredictionEvaluationSampleCount();
}
