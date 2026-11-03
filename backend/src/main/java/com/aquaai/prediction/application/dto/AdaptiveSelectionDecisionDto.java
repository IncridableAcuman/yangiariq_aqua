package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.AdaptiveSelectionDecision;
import com.aquaai.prediction.domain.model.ContextMatchLevel;
import com.aquaai.prediction.domain.model.PredictionType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AdaptiveSelectionDecisionDto(
        UUID id,
        UUID predictionId,
        UUID fieldId,
        String fieldName,
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
    public static AdaptiveSelectionDecisionDto fromDomain(AdaptiveSelectionDecision d, String fieldName) {
        if (d == null) return null;
        return AdaptiveSelectionDecisionDto.builder()
                .id(d.getId())
                .predictionId(d.getPredictionId())
                .fieldId(d.getFieldId())
                .fieldName(fieldName != null ? fieldName : "Field " + d.getFieldId().toString().substring(0, 8))
                .predictionType(d.getPredictionType())
                .selectedVersionTag(d.getSelectedVersionTag())
                .selectionMode(d.getSelectionMode())
                .selectionReason(d.getSelectionReason())
                .evidenceSummary(d.getEvidenceSummary())
                .contextSnapshotJson(d.getContextSnapshotJson())
                .alternativesJson(d.getAlternativesJson())
                .confidencePercent(d.getConfidencePercent())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
