package com.aquaai.risk.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable atomic evidence supporting a risk factor.
 * Links directly to observed telemetry, water budget balances, anomalies, or predictions.
 */
public record RiskEvidence(
        UUID id,
        RiskCategory category,
        String metricName,
        BigDecimal observedValue,
        BigDecimal thresholdValue,
        String unit,
        RiskEvidenceType evidenceType,
        RiskEvidenceSource source,
        String message,
        Instant recordedAt,
        boolean isAuthoritative
) {
    public RiskEvidence {
        id = id != null ? id : UUID.randomUUID();
        category = Objects.requireNonNull(category, "RiskCategory must not be null");
        metricName = Objects.requireNonNull(metricName, "metricName must not be null");
        evidenceType = Objects.requireNonNull(evidenceType, "evidenceType must not be null");
        source = Objects.requireNonNull(source, "source must not be null");
        message = Objects.requireNonNull(message, "message must not be null");
        recordedAt = recordedAt != null ? recordedAt : Instant.now();
    }

    public static RiskEvidence of(
            RiskCategory category,
            String metricName,
            BigDecimal observedValue,
            BigDecimal thresholdValue,
            String unit,
            RiskEvidenceType evidenceType,
            RiskEvidenceSource source,
            String message,
            boolean isAuthoritative
    ) {
        return new RiskEvidence(
                UUID.randomUUID(),
                category,
                metricName,
                observedValue,
                thresholdValue,
                unit,
                evidenceType,
                source,
                message,
                Instant.now(),
                isAuthoritative
        );
    }
}
