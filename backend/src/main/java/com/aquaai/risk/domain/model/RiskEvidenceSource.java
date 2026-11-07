package com.aquaai.risk.domain.model;

/**
 * Underlying platform subsystem from which the evidence was gathered.
 */
public enum RiskEvidenceSource {
    TELEMETRY,
    WATER_BUDGET,
    HYDRAULIC_SAFETY,
    IRRIGATION_OPERATION,
    PREDICTION_ENGINE,
    MODEL_GOVERNANCE
}
