package com.aquaai.recommendation.api.dto;

import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.ConfidenceLevel;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationSource;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
        UUID id,
        UUID fieldId,
        RecommendationType recommendationType,
        RecommendationPriority priority,
        RecommendationStatus status,
        String title,
        String summary,
        String explanation,
        BigDecimal confidence,
        ConfidenceLevel confidenceLevel,
        Instant validFrom,
        Instant validUntil,
        Instant generatedAt,
        RecommendationSource source,
        String limitations,
        boolean isSimulated,
        Instant viewedAt,
        UUID viewedBy,
        Instant acceptedAt,
        UUID acceptedBy,
        Instant dismissedAt,
        UUID dismissedBy,
        String dismissalReason,
        Instant completedAt,
        UUID completedBy,
        List<RecommendationEvidenceResponse> evidence,
        List<RecommendedActionResponse> actions,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
    public static RecommendationResponse fromDomain(AgronomicRecommendation domain) {
        if (domain == null) {
            return null;
        }

        List<RecommendationEvidenceResponse> evidenceList = domain.getEvidence() != null
                ? domain.getEvidence().stream().map(RecommendationEvidenceResponse::fromDomain).toList()
                : Collections.emptyList();

        List<RecommendedActionResponse> actionList = domain.getActions() != null
                ? domain.getActions().stream().map(RecommendedActionResponse::fromDomain).toList()
                : Collections.emptyList();

        return new RecommendationResponse(
                domain.getId(),
                domain.getFieldId(),
                domain.getRecommendationType(),
                domain.getPriority(),
                domain.getStatus(),
                domain.getTitle(),
                domain.getSummary(),
                domain.getExplanation(),
                domain.getConfidence(),
                domain.getConfidenceLevel(),
                domain.getValidFrom(),
                domain.getValidUntil(),
                domain.getGeneratedAt(),
                domain.getSource(),
                domain.getLimitations(),
                domain.isSimulated(),
                domain.getViewedAt(),
                domain.getViewedBy(),
                domain.getAcceptedAt(),
                domain.getAcceptedBy(),
                domain.getDismissedAt(),
                domain.getDismissedBy(),
                domain.getDismissalReason(),
                domain.getCompletedAt(),
                domain.getCompletedBy(),
                evidenceList,
                actionList,
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getVersion()
        );
    }
}
