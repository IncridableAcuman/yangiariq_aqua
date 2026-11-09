package com.aquaai.integration.infrastructure.persistence.mapper;

import com.aquaai.integration.domain.model.ImportBatch;
import com.aquaai.integration.domain.model.ImportRowResult;
import com.aquaai.integration.infrastructure.persistence.entity.ImportBatchJpaEntity;
import com.aquaai.integration.infrastructure.persistence.entity.ImportRowResultJpaEntity;

public final class ImportBatchJpaMapper {

    private ImportBatchJpaMapper() {}

    public static ImportBatch toDomain(ImportBatchJpaEntity entity) {
        if (entity == null) return null;
        return new ImportBatch(
                entity.getId(),
                entity.getDataSourceId(),
                entity.getFileName(),
                entity.getFileSizeBytes(),
                entity.getTotalRows(),
                entity.getSuccessfulRows(),
                entity.getRejectedRows(),
                entity.getStatus(),
                entity.getUploadedBy(),
                entity.getStartedAt(),
                entity.getCompletedAt()
        );
    }

    public static ImportBatchJpaEntity toEntity(ImportBatch domain) {
        if (domain == null) return null;
        return ImportBatchJpaEntity.builder()
                .id(domain.getId())
                .dataSourceId(domain.getDataSourceId())
                .fileName(domain.getFileName())
                .fileSizeBytes(domain.getFileSizeBytes())
                .totalRows(domain.getTotalRows())
                .successfulRows(domain.getSuccessfulRows())
                .rejectedRows(domain.getRejectedRows())
                .status(domain.getStatus())
                .uploadedBy(domain.getUploadedBy())
                .startedAt(domain.getStartedAt())
                .completedAt(domain.getCompletedAt())
                .build();
    }

    public static ImportRowResult toDomain(ImportRowResultJpaEntity entity) {
        if (entity == null) return null;
        return new ImportRowResult(
                entity.getId(),
                entity.getBatchId(),
                entity.getRowNumber(),
                entity.isSuccess(),
                entity.getExternalRecordId(),
                entity.getErrorMessage(),
                entity.getCreatedAt()
        );
    }

    public static ImportRowResultJpaEntity toEntity(ImportRowResult domain) {
        if (domain == null) return null;
        return ImportRowResultJpaEntity.builder()
                .id(domain.getId())
                .batchId(domain.getBatchId())
                .rowNumber(domain.getRowNumber())
                .isSuccess(domain.isSuccess())
                .externalRecordId(domain.getExternalRecordId())
                .errorMessage(domain.getErrorMessage())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
