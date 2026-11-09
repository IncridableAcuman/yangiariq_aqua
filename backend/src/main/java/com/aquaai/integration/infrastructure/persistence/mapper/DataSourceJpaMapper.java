package com.aquaai.integration.infrastructure.persistence.mapper;

import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.infrastructure.persistence.entity.DataSourceJpaEntity;

public final class DataSourceJpaMapper {

    private DataSourceJpaMapper() {}

    public static DataSource toDomain(DataSourceJpaEntity entity) {
        if (entity == null) return null;
        return new DataSource(
                entity.getId(),
                entity.getSourceCode(),
                entity.getName(),
                entity.getSourceType(),
                entity.getProviderName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getEndpointUrl(),
                entity.getConfigurationRef(),
                entity.getLastSyncAt(),
                entity.getLastSuccessAt(),
                entity.getLastErrorAt(),
                entity.getLastErrorMessage(),
                entity.getTotalIngestedCount(),
                entity.getTotalRejectedCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static DataSourceJpaEntity toEntity(DataSource domain) {
        if (domain == null) return null;
        return DataSourceJpaEntity.builder()
                .id(domain.getId())
                .sourceCode(domain.getSourceCode())
                .name(domain.getName())
                .sourceType(domain.getSourceType())
                .providerName(domain.getProviderName())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .endpointUrl(domain.getEndpointUrl())
                .configurationRef(domain.getConfigurationRef())
                .lastSyncAt(domain.getLastSyncAt())
                .lastSuccessAt(domain.getLastSuccessAt())
                .lastErrorAt(domain.getLastErrorAt())
                .lastErrorMessage(domain.getLastErrorMessage())
                .totalIngestedCount(domain.getTotalIngestedCount())
                .totalRejectedCount(domain.getTotalRejectedCount())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
