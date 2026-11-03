package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.AdaptiveSelectionDecision;
import com.aquaai.prediction.domain.model.ContextMatchLevel;
import com.aquaai.prediction.domain.model.PredictionType;

public class PredictionAdaptiveJpaMapper {

    public static AdaptiveSelectionDecision toDomain(AdaptiveSelectionDecisionJpaEntity entity) {
        if (entity == null) return null;
        return new AdaptiveSelectionDecision(
                entity.getId(),
                entity.getPredictionId(),
                entity.getFieldId(),
                PredictionType.valueOf(entity.getPredictionType()),
                entity.getSelectedVersionTag(),
                ContextMatchLevel.valueOf(entity.getSelectionMode()),
                entity.getSelectionReason(),
                entity.getEvidenceSummary(),
                entity.getContextSnapshotJson(),
                entity.getAlternativesJson(),
                entity.getConfidencePercent(),
                entity.getCreatedAt()
        );
    }

    public static AdaptiveSelectionDecisionJpaEntity toJpa(AdaptiveSelectionDecision domain) {
        if (domain == null) return null;
        return AdaptiveSelectionDecisionJpaEntity.builder()
                .id(domain.getId())
                .predictionId(domain.getPredictionId())
                .fieldId(domain.getFieldId())
                .predictionType(domain.getPredictionType().name())
                .selectedVersionTag(domain.getSelectedVersionTag())
                .selectionMode(domain.getSelectionMode().name())
                .selectionReason(domain.getSelectionReason())
                .evidenceSummary(domain.getEvidenceSummary())
                .contextSnapshotJson(domain.getContextSnapshotJson())
                .alternativesJson(domain.getAlternativesJson())
                .confidencePercent(domain.getConfidencePercent())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
