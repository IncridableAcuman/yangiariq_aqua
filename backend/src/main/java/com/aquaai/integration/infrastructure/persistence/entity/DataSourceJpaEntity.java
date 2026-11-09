package com.aquaai.integration.infrastructure.persistence.entity;

import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "data_sources")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "source_code", nullable = false, unique = true, length = 64)
    private String sourceCode;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private DataSourceType sourceType;

    @Column(name = "provider_name", nullable = false, length = 64)
    private String providerName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DataSourceStatus status;

    @Column(name = "endpoint_url", length = 255)
    private String endpointUrl;

    @Column(name = "configuration_ref", length = 128)
    private String configurationRef;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "last_error_at")
    private Instant lastErrorAt;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Column(name = "total_ingested_count", nullable = false)
    private long totalIngestedCount;

    @Column(name = "total_rejected_count", nullable = false)
    private long totalRejectedCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
