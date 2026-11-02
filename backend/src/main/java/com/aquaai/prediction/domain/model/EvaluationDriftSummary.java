package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;

/**
 * Temporal comparison between current evaluation period and preceding comparable period.
 */
public record EvaluationDriftSummary(
        String metricName,
        BigDecimal currentPeriodMetric,
        BigDecimal previousPeriodMetric,
        BigDecimal percentageChange,
        boolean driftDetected,
        ModelHealthStatus status,
        String explanation
) {}
