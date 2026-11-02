package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Audit record capturing the decision-support pipeline:
 * Forecast -> Recommendation -> Human Decision (approved) -> Actual Operation -> Outcome.
 */
public record HumanDecisionImpact(
        UUID predictionId,
        UUID fieldId,
        String fieldName,
        Instant targetTime,
        BigDecimal forecastValueM3,
        BigDecimal recommendedValueM3,
        BigDecimal approvedValueM3,
        BigDecimal actualOutcomeValueM3,
        BigDecimal decisionDeltaM3, // approved - recommended
        BigDecimal outcomeDeltaM3,  // actual - approved
        String operationalContextNotes
) {}
