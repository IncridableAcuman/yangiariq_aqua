package com.aquaai.anomaly.api.dto;

import com.aquaai.anomaly.domain.model.AnomalyEvidence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyEvidenceResponse(
        UUID id,
        String evidenceType,
        String metric,
        BigDecimal observedValue,
        BigDecimal expectedValue,
        Instant measuredAt,
        String metadata
) {
    public static AnomalyEvidenceResponse fromDomain(AnomalyEvidence domain) {
        if (domain == null) return null;
        return new AnomalyEvidenceResponse(
                domain.getId(),
                domain.getEvidenceType(),
                domain.getMetric(),
                domain.getObservedValue(),
                domain.getExpectedValue(),
                domain.getMeasuredAt(),
                domain.getMetadata()
        );
    }
}
