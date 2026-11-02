package com.aquaai.prediction.domain.model;

/**
 * Actionable insight generated from predictive model evaluation and accuracy analysis.
 */
public record EvaluationInsight(
        String id,
        String category, // BIAS, CALIBRATION, TELEMETRY, SAFETY, DRIFT
        String severity, // INFO, WARNING, CRITICAL
        String title,
        String description,
        String evidence,
        String recommendedAction,
        String actionTargetModule,
        String actionTargetId
) {}
