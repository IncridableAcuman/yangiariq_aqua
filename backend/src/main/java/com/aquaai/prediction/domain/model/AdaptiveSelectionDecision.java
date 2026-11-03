package com.aquaai.prediction.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable audit entity recording an authoritative context-aware adaptive selection decision.
 */
@Getter
@Builder
public class AdaptiveSelectionDecision {

    private final UUID id;
    private final UUID predictionId;
    private final UUID fieldId;
    private final PredictionType predictionType;
    private final String selectedVersionTag;
    private final ContextMatchLevel selectionMode;
    private final String selectionReason;
    private final String evidenceSummary;
    private final String contextSnapshotJson;
    private final String alternativesJson;
    private final int confidencePercent;
    private final Instant createdAt;

    public AdaptiveSelectionDecision(
            UUID id,
            UUID predictionId,
            UUID fieldId,
            PredictionType predictionType,
            String selectedVersionTag,
            ContextMatchLevel selectionMode,
            String selectionReason,
            String evidenceSummary,
            String contextSnapshotJson,
            String alternativesJson,
            int confidencePercent,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.predictionId = predictionId;
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.predictionType = Objects.requireNonNull(predictionType, "predictionType must not be null");
        this.selectedVersionTag = Objects.requireNonNull(selectedVersionTag, "selectedVersionTag must not be null");
        this.selectionMode = selectionMode != null ? selectionMode : ContextMatchLevel.BASELINE_FALLBACK;
        this.selectionReason = Objects.requireNonNull(selectionReason, "selectionReason must not be null");
        this.evidenceSummary = evidenceSummary != null ? evidenceSummary : "";
        this.contextSnapshotJson = Objects.requireNonNull(contextSnapshotJson, "contextSnapshotJson must not be null");
        this.alternativesJson = alternativesJson != null ? alternativesJson : "[]";
        this.confidencePercent = confidencePercent;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static AdaptiveSelectionDecision create(
            UUID predictionId,
            AdaptiveSelectionResult result,
            String contextSnapshotJson,
            String alternativesJson
    ) {
        return new AdaptiveSelectionDecision(
                UUID.randomUUID(),
                predictionId,
                result.fieldId(),
                result.predictionType(),
                result.selectedVersionTag(),
                result.contextMatchLevel(),
                result.selectionReason(),
                result.evidenceSummary(),
                contextSnapshotJson,
                alternativesJson,
                result.confidencePercent(),
                Instant.now()
        );
    }
}
