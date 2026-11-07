package com.aquaai.risk.api.dto;

import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskRecommendation;
import com.aquaai.risk.domain.model.RiskSeverity;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record RiskRecommendationResponse(
        UUID id,
        UUID assessmentId,
        UUID fieldId,
        String fieldName,
        String title,
        String description,
        RiskCategory category,
        RiskSeverity severity,
        RiskConfidence confidence,
        List<RiskEvidenceResponse> supportingEvidence,
        String reason,
        String suggestedNextAction,
        int priorityRank,
        String relatedOperationalEntity,
        String destinationRoute,
        Instant createdAt
) {
    public static RiskRecommendationResponse fromDomain(RiskRecommendation r) {
        if (r == null) return null;
        List<RiskEvidenceResponse> evList = r.supportingEvidence() != null
                ? r.supportingEvidence().stream().map(RiskEvidenceResponse::fromDomain).toList()
                : Collections.emptyList();

        return new RiskRecommendationResponse(
                r.id(),
                r.assessmentId(),
                r.fieldId(),
                r.fieldName(),
                r.title(),
                r.description(),
                r.category(),
                r.severity(),
                r.confidence(),
                evList,
                r.reason(),
                r.suggestedNextAction(),
                r.priorityRank(),
                r.relatedOperationalEntity(),
                r.destinationRoute(),
                r.createdAt()
        );
    }
}
