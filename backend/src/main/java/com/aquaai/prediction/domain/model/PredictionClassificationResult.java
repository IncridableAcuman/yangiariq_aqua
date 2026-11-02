package com.aquaai.prediction.domain.model;

/**
 * Standard confusion-matrix classification result for categorical or binary forecasts
 * (e.g., Irrigation Need, Risk Level).
 */
public enum PredictionClassificationResult {
    TRUE_POSITIVE,
    TRUE_NEGATIVE,
    FALSE_POSITIVE,
    FALSE_NEGATIVE,
    INTERRUPTED,
    NOT_APPLICABLE
}
