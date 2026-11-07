package com.aquaai.wateraccounting.infrastructure.persistence.mapper;

import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.ReconciliationStatus;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import com.aquaai.wateraccounting.infrastructure.persistence.entity.WaterReconciliationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class WaterReconciliationJpaMapper {

    public WaterReconciliationItem toDomain(WaterReconciliationJpaEntity entity) {
        if (entity == null) return null;

        return WaterReconciliationItem.builder()
                .id(entity.getId())
                .boundaryType(entity.getBoundaryType())
                .boundaryName(entity.getBoundaryName())
                .targetId(entity.getTargetId())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .expectedVolumeM3(entity.getExpectedVolumeM3())
                .recordedVolumeM3(entity.getRecordedVolumeM3())
                .discrepancyVolumeM3(entity.getDiscrepancyVolumeM3())
                .discrepancyPercent(entity.getDiscrepancyPercent())
                .tolerancePercent(entity.getTolerancePercent())
                .status(ReconciliationStatus.valueOf(entity.getStatus()))
                .evidenceClassification(EvidenceClassification.valueOf(entity.getEvidenceClassification()))
                .explanation(entity.getExplanation())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .reviewNotes(entity.getReviewNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public WaterReconciliationJpaEntity toEntity(WaterReconciliationItem domain) {
        if (domain == null) return null;

        return WaterReconciliationJpaEntity.builder()
                .id(domain.getId())
                .boundaryType(domain.getBoundaryType())
                .boundaryName(domain.getBoundaryName())
                .targetId(domain.getTargetId())
                .periodStart(domain.getPeriodStart())
                .periodEnd(domain.getPeriodEnd())
                .expectedVolumeM3(domain.getExpectedVolumeM3())
                .recordedVolumeM3(domain.getRecordedVolumeM3())
                .discrepancyVolumeM3(domain.getDiscrepancyVolumeM3())
                .discrepancyPercent(domain.getDiscrepancyPercent())
                .tolerancePercent(domain.getTolerancePercent())
                .status(domain.getStatus().name())
                .evidenceClassification(domain.getEvidenceClassification().name())
                .explanation(domain.getExplanation())
                .reviewedBy(domain.getReviewedBy())
                .reviewedAt(domain.getReviewedAt())
                .reviewNotes(domain.getReviewNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
