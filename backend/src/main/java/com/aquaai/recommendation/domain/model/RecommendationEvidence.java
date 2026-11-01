package com.aquaai.recommendation.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Empirical corroborating evidence supporting an agronomic recommendation.
 */
@Getter
public class RecommendationEvidence {

    private final UUID id;
    private final String evidenceType;
    private final String metric;
    private final BigDecimal observedValue;
    private final BigDecimal baselineValue;
    private final String unit;
    private final String summary;
    private final String sourceReference;

    public RecommendationEvidence(
            UUID id,
            String evidenceType,
            String metric,
            BigDecimal observedValue,
            BigDecimal baselineValue,
            String unit,
            String summary,
            String sourceReference
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.evidenceType = Objects.requireNonNull(evidenceType, "Evidence type cannot be null");
        this.metric = Objects.requireNonNull(metric, "Metric cannot be null");
        this.observedValue = observedValue;
        this.baselineValue = baselineValue;
        this.unit = unit;
        this.summary = Objects.requireNonNull(summary, "Evidence summary cannot be null");
        this.sourceReference = sourceReference;
    }

    public static RecommendationEvidence of(
            String evidenceType,
            String metric,
            BigDecimal observedValue,
            BigDecimal baselineValue,
            String unit,
            String summary
    ) {
        return new RecommendationEvidence(UUID.randomUUID(), evidenceType, metric, observedValue, baselineValue, unit, summary, null);
    }
}
