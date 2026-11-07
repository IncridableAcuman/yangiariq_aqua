package com.aquaai.risk.api.dto;

import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskEvidence;
import com.aquaai.risk.domain.model.RiskEvidenceSource;
import com.aquaai.risk.domain.model.RiskEvidenceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RiskEvidenceResponse(
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
    public static RiskEvidenceResponse fromDomain(RiskEvidence e) {
        if (e == null) return null;
        return new RiskEvidenceResponse(
                e.id(),
                e.category(),
                e.metricName(),
                e.observedValue(),
                e.thresholdValue(),
                e.unit(),
                e.evidenceType(),
                e.source(),
                e.message(),
                e.recordedAt(),
                e.isAuthoritative()
        );
    }
}
