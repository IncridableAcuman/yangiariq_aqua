package com.aquaai.integration.application.dto;

import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;

import java.time.Instant;
import java.util.UUID;

public record DataSourceDto(
        UUID id,
        String sourceCode,
        String name,
        DataSourceType sourceType,
        String providerName,
        String description,
        DataSourceStatus status,
        String endpointUrl,
        String configurationRef,
        Instant lastSyncAt,
        Instant lastSuccessAt,
        Instant lastErrorAt,
        String lastErrorMessage,
        long totalIngestedCount,
        long totalRejectedCount,
        double reliabilityRate,
        Instant createdAt,
        Instant updatedAt
) {
    public static DataSourceDto from(DataSource domain) {
        if (domain == null) return null;
        long total = domain.getTotalIngestedCount() + domain.getTotalRejectedCount();
        double reliability = total > 0 ? (double) domain.getTotalIngestedCount() / total * 100.0 : 100.0;
        return new DataSourceDto(
                domain.getId(),
                domain.getSourceCode(),
                domain.getName(),
                domain.getSourceType(),
                domain.getProviderName(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getEndpointUrl(),
                domain.getConfigurationRef(),
                domain.getLastSyncAt(),
                domain.getLastSuccessAt(),
                domain.getLastErrorAt(),
                domain.getLastErrorMessage(),
                domain.getTotalIngestedCount(),
                domain.getTotalRejectedCount(),
                Math.round(reliability * 10.0) / 10.0,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
