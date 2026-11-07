package com.aquaai.risk.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * An actionable, explainable operational recommendation triggered by verified evidence.
 * Preserves human agency: recommendations advise but never bypass human operational approval.
 */
public record RiskRecommendation(
        UUID id,
        UUID assessmentId,
        UUID fieldId,
        String fieldName,
        String title,
        String description,
        RiskCategory category,
        RiskSeverity severity,
        RiskConfidence confidence,
        List<RiskEvidence> supportingEvidence,
        String reason,
        String suggestedNextAction,
        int priorityRank,
        String relatedOperationalEntity,
        String destinationRoute,
        Instant createdAt
) {
    public RiskRecommendation {
        id = id != null ? id : UUID.randomUUID();
        fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        fieldName = fieldName != null ? fieldName : "Field";
        title = Objects.requireNonNull(title, "title must not be null");
        description = Objects.requireNonNull(description, "description must not be null");
        category = Objects.requireNonNull(category, "category must not be null");
        severity = Objects.requireNonNull(severity, "severity must not be null");
        confidence = Objects.requireNonNull(confidence, "confidence must not be null");
        supportingEvidence = supportingEvidence != null ? Collections.unmodifiableList(supportingEvidence) : Collections.emptyList();
        reason = reason != null ? reason : "";
        suggestedNextAction = suggestedNextAction != null ? suggestedNextAction : "";
        createdAt = createdAt != null ? createdAt : Instant.now();
    }
}
