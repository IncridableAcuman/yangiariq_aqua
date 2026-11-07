package com.aquaai.wateraccounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Explicit evidence metadata backing every water accounting figure.
 */
public record WaterAccountingEvidence(
        BigDecimal value,
        String unit,
        Instant timestamp,
        String source,
        String dataQuality,
        EvidenceClassification classification,
        String networkComponent,
        UUID relatedId
) {
    public static WaterAccountingEvidence observed(BigDecimal value, String unit, Instant timestamp, String source, String networkComponent, UUID relatedId) {
        return new WaterAccountingEvidence(value, unit, timestamp, source, "HIGH", EvidenceClassification.OBSERVED, networkComponent, relatedId);
    }

    public static WaterAccountingEvidence calculated(BigDecimal value, String unit, Instant timestamp, String source, String networkComponent, UUID relatedId) {
        return new WaterAccountingEvidence(value, unit, timestamp, source, "VERIFIED", EvidenceClassification.CALCULATED, networkComponent, relatedId);
    }

    public static WaterAccountingEvidence estimated(BigDecimal value, String unit, Instant timestamp, String source, String networkComponent, UUID relatedId) {
        return new WaterAccountingEvidence(value, unit, timestamp, source, "MEDIUM", EvidenceClassification.ESTIMATED, networkComponent, relatedId);
    }

    public static WaterAccountingEvidence simulated(BigDecimal value, String unit, Instant timestamp, String source, String networkComponent, UUID relatedId) {
        return new WaterAccountingEvidence(value, unit, timestamp, source, "SIMULATED", EvidenceClassification.SIMULATED, networkComponent, relatedId);
    }

    public static WaterAccountingEvidence missing(String unit, String networkComponent, UUID relatedId) {
        return new WaterAccountingEvidence(BigDecimal.ZERO, unit, Instant.now(), "UNAVAILABLE", "NONE", EvidenceClassification.MISSING, networkComponent, relatedId);
    }
}
