package com.aquaai.prediction.domain.model;

/**
 * Lifecycle status of predictive model evaluation.
 */
public enum EvaluationStatus {
    /**
     * Prediction target horizon has not yet closed or outcome has not yet been processed.
     */
    PENDING,

    /**
     * Prediction has been matched with authoritative outcome and evaluated.
     */
    EVALUATED,

    /**
     * Target horizon closed, but required outcome telemetry or observations are missing.
     */
    INSUFFICIENT_DATA,

    /**
     * Operation was interrupted, cancelled, or prevented by a safety interlock (e.g., HydraulicSafetyInterlock).
     */
    OUTCOME_INTERRUPTED,

    /**
     * Target horizon expired without evaluatable observation window or exceeded tolerance.
     */
    EXPIRED,

    /**
     * Corrupted, uncalibrated, or invalid forecast.
     */
    INVALID
}
