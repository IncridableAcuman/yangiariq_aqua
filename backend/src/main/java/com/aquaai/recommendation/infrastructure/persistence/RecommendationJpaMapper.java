package com.aquaai.recommendation.infrastructure.persistence;

import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationEvidence;
import com.aquaai.recommendation.domain.model.RecommendedAction;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RecommendationJpaMapper {

    public RecommendationJpaEntity toEntity(AgronomicRecommendation domain) {
        if (domain == null) {
            return null;
        }

        RecommendationJpaEntity entity = new RecommendationJpaEntity();
        entity.setId(domain.getId());
        entity.setFieldId(domain.getFieldId());
        entity.setRecommendationType(domain.getRecommendationType());
        entity.setPriority(domain.getPriority());
        entity.setStatus(domain.getStatus());
        entity.setTitle(domain.getTitle());
        entity.setSummary(domain.getSummary());
        entity.setExplanation(domain.getExplanation());
        entity.setConfidence(domain.getConfidence());
        entity.setConfidenceLevel(domain.getConfidenceLevel());
        entity.setValidFrom(domain.getValidFrom());
        entity.setValidUntil(domain.getValidUntil());
        entity.setGeneratedAt(domain.getGeneratedAt());
        entity.setSource(domain.getSource());
        entity.setLimitations(domain.getLimitations());
        entity.setSimulated(domain.isSimulated());

        entity.setViewedAt(domain.getViewedAt());
        entity.setViewedBy(domain.getViewedBy());
        entity.setAcceptedAt(domain.getAcceptedAt());
        entity.setAcceptedBy(domain.getAcceptedBy());
        entity.setDismissedAt(domain.getDismissedAt());
        entity.setDismissedBy(domain.getDismissedBy());
        entity.setDismissalReason(domain.getDismissalReason());
        entity.setCompletedAt(domain.getCompletedAt());
        entity.setCompletedBy(domain.getCompletedBy());

        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());

        if (domain.getEvidence() != null) {
            List<RecommendationEvidenceJpaEntity> evidenceEntities = domain.getEvidence().stream()
                    .map(e -> new RecommendationEvidenceJpaEntity(
                            e.getId(),
                            domain.getId(),
                            e.getEvidenceType(),
                            e.getMetric(),
                            e.getObservedValue(),
                            e.getBaselineValue(),
                            e.getUnit(),
                            e.getSummary(),
                            e.getSourceReference(),
                            domain.getCreatedAt()
                    ))
                    .toList();
            entity.getEvidence().addAll(evidenceEntities);
        }

        if (domain.getActions() != null) {
            List<RecommendedActionJpaEntity> actionEntities = domain.getActions().stream()
                    .map(a -> new RecommendedActionJpaEntity(
                            a.getId(),
                            domain.getId(),
                            a.getActionOrder(),
                            a.getActionText(),
                            a.getUrgency(),
                            a.isNegativeAction(),
                            domain.getCreatedAt()
                    ))
                    .toList();
            entity.getActions().addAll(actionEntities);
        }

        return entity;
    }

    public AgronomicRecommendation toDomain(RecommendationJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<RecommendationEvidence> evidenceList = entity.getEvidence() != null
                ? entity.getEvidence().stream()
                .map(e -> new RecommendationEvidence(
                        e.getId(),
                        e.getEvidenceType(),
                        e.getMetric(),
                        e.getObservedValue(),
                        e.getBaselineValue(),
                        e.getUnit(),
                        e.getSummary(),
                        e.getSourceReference()
                ))
                .toList()
                : Collections.emptyList();

        List<RecommendedAction> actionList = entity.getActions() != null
                ? entity.getActions().stream()
                .map(a -> new RecommendedAction(
                        a.getId(),
                        a.getActionOrder(),
                        a.getActionText(),
                        a.getUrgency(),
                        a.isNegativeAction()
                ))
                .toList()
                : Collections.emptyList();

        return AgronomicRecommendation.reconstitute(
                entity.getId(),
                entity.getFieldId(),
                entity.getRecommendationType(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getExplanation(),
                entity.getConfidence(),
                entity.getConfidenceLevel(),
                entity.getValidFrom(),
                entity.getValidUntil(),
                entity.getGeneratedAt(),
                entity.getSource(),
                entity.getLimitations(),
                entity.isSimulated(),
                entity.getViewedAt(),
                entity.getViewedBy(),
                entity.getAcceptedAt(),
                entity.getAcceptedBy(),
                entity.getDismissedAt(),
                entity.getDismissedBy(),
                entity.getDismissalReason(),
                entity.getCompletedAt(),
                entity.getCompletedBy(),
                evidenceList,
                actionList,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
