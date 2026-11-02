package com.aquaai.prediction.domain.model;

/**
 * Standard classification of evaluation limitations and failure causes.
 * <p>
 * Ensures errors stemming from missing telemetry, cancelled operations, or safety events
 * are not falsely attributed to algorithmic model failure.
 */
public enum EvaluationFailureReason {
    NONE,
    INSUFFICIENT_DATA,
    STALE_TELEMETRY,
    MISSING_SENSOR,
    MISSING_OUTCOME,
    OUTCOME_OUTSIDE_WINDOW,
    LOW_SAMPLE_SIZE,
    HIGH_VARIANCE,
    SAFETY_EVENT,
    OPERATION_CANCELLED,
    WEATHER_DATA_MISSING,
    OTHER
}
