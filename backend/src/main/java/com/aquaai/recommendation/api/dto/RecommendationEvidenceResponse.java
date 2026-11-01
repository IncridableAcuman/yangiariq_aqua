package com.aquaai.recommendation.api.dto;

import com.aquaai.recommendation.domain.model.RecommendationEvidence;

import java.math.BigDecimal;
import java.util.UUID;

public record RecommendationEvidenceResponse(
        UUID id,
        String evidenceType,
        String metric,
        BigDecimal observedValue,
        BigDecimal baselineValue,
        String unit,
        String summary,
        String sourceReference
) {
    public static RecommendationEvidenceResponse fromDomain(RecommendationEvidence evidence) {
        if (evidence == null) {
            return null;
        }
        return new RecommendationEvidenceResponse(
                evidence.getId(),
                evidence.getEvidenceType(),
                evidence.getMetric(),
                evidence.getObservedValue(),
                evidence.getBaselineValue(),
                evidence.getUnit(),
                evidence.getSummary(),
                evidence.getSourceReference()
        );
    }
}
