package com.aquaai.anomaly.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable value object representing a physical metric or observation
 * corroborating an identified anomaly.
 */
@Getter
public class AnomalyEvidence {

    private final UUID id;
    private final String evidenceType;
    private final String metric;
    private final BigDecimal observedValue;
    private final BigDecimal expectedValue;
    private final Instant measuredAt;
    private final String metadata;

    public AnomalyEvidence(
            UUID id,
            String evidenceType,
            String metric,
            BigDecimal observedValue,
            BigDecimal expectedValue,
            Instant measuredAt,
            String metadata
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.evidenceType = Objects.requireNonNull(evidenceType, "evidenceType must not be null");
        this.metric = Objects.requireNonNull(metric, "metric must not be null");
        this.observedValue = observedValue;
        this.expectedValue = expectedValue;
        this.measuredAt = measuredAt != null ? measuredAt : Instant.now();
        this.metadata = metadata;
    }

    public static AnomalyEvidence of(
            String evidenceType,
            String metric,
            BigDecimal observedValue,
            BigDecimal expectedValue,
            Instant measuredAt,
            String metadata
    ) {
        return new AnomalyEvidence(UUID.randomUUID(), evidenceType, metric, observedValue, expectedValue, measuredAt, metadata);
    }
}
