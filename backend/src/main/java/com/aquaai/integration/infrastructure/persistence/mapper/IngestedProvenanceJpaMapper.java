package com.aquaai.integration.infrastructure.persistence.mapper;

import com.aquaai.integration.domain.model.IngestedProvenance;
import com.aquaai.integration.infrastructure.persistence.entity.IngestedProvenanceJpaEntity;

public final class IngestedProvenanceJpaMapper {

    private IngestedProvenanceJpaMapper() {}

    public static IngestedProvenance toDomain(IngestedProvenanceJpaEntity entity) {
        if (entity == null) return null;
        return new IngestedProvenance(
                entity.getId(),
                entity.getDataSourceId(),
                entity.getExternalRecordId(),
                entity.getIdempotencyKey(),
                entity.getSensorReadingId(),
                entity.getFieldId(),
                entity.getSensorId(),
                entity.getSensorType(),
                entity.getRawValue(),
                entity.getRawUnit(),
                entity.getNormalizedValue(),
                entity.getCanonicalUnit(),
                entity.getQualityStatus(),
                entity.getEvidenceClassification(),
                entity.getValidationMessage(),
                entity.getMeasuredAt(),
                entity.getIngestedAt()
        );
    }

    public static IngestedProvenanceJpaEntity toEntity(IngestedProvenance domain) {
        if (domain == null) return null;
        return IngestedProvenanceJpaEntity.builder()
                .id(domain.getId())
                .dataSourceId(domain.getDataSourceId())
                .externalRecordId(domain.getExternalRecordId())
                .idempotencyKey(domain.getIdempotencyKey())
                .sensorReadingId(domain.getSensorReadingId())
                .fieldId(domain.getFieldId())
                .sensorId(domain.getSensorId())
                .sensorType(domain.getSensorType())
                .rawValue(domain.getRawValue())
                .rawUnit(domain.getRawUnit())
                .normalizedValue(domain.getNormalizedValue())
                .canonicalUnit(domain.getCanonicalUnit())
                .qualityStatus(domain.getQualityStatus())
                .evidenceClassification(domain.getEvidenceClassification())
                .validationMessage(domain.getValidationMessage())
                .measuredAt(domain.getMeasuredAt())
                .ingestedAt(domain.getIngestedAt())
                .build();
    }
}
