package com.aquaai.integration.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain aggregate representing an external or internal source of telemetry and agronomic data.
 */
public class DataSource {

    private final UUID id;
    private String sourceCode;
    private String name;
    private DataSourceType sourceType;
    private String providerName;
    private String description;
    private DataSourceStatus status;
    private String endpointUrl;
    private String configurationRef;
    private Instant lastSyncAt;
    private Instant lastSuccessAt;
    private Instant lastErrorAt;
    private String lastErrorMessage;
    private long totalIngestedCount;
    private long totalRejectedCount;
    private final Instant createdAt;
    private Instant updatedAt;

    public static DataSource create(
            String sourceCode,
            String name,
            DataSourceType sourceType,
            String providerName,
            String description,
            String endpointUrl,
            String configurationRef,
            DataSourceStatus initialStatus
    ) {
        Instant now = Instant.now();
        return new DataSource(
                UUID.randomUUID(),
                sourceCode,
                name,
                sourceType,
                providerName,
                description,
                initialStatus != null ? initialStatus : DataSourceStatus.ACTIVE,
                endpointUrl,
                configurationRef,
                null,
                null,
                null,
                null,
                0L,
                0L,
                now,
                now
        );
    }

    public DataSource(
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
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Data source ID must not be null");
        this.sourceCode = validateSourceCode(sourceCode);
        this.name = Objects.requireNonNull(name, "Data source name must not be null").trim();
        this.sourceType = Objects.requireNonNull(sourceType, "Data source type must not be null");
        this.providerName = Objects.requireNonNull(providerName, "Provider name must not be null").trim();
        this.description = description != null ? description.trim() : null;
        this.status = status != null ? status : DataSourceStatus.ACTIVE;
        this.endpointUrl = sanitizeEndpointUrl(endpointUrl);
        this.configurationRef = configurationRef != null ? configurationRef.trim() : null;
        this.lastSyncAt = lastSyncAt;
        this.lastSuccessAt = lastSuccessAt;
        this.lastErrorAt = lastErrorAt;
        this.lastErrorMessage = lastErrorMessage;
        this.totalIngestedCount = Math.max(0, totalIngestedCount);
        this.totalRejectedCount = Math.max(0, totalRejectedCount);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    private static String validateSourceCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Data source code must not be blank");
        }
        String clean = code.trim().toUpperCase();
        if (!clean.matches("^[A-Z0-9_-]{3,64}$")) {
            throw new IllegalArgumentException("Data source code must contain 3-64 alphanumeric characters, underscores, or hyphens: " + code);
        }
        return clean;
    }

    private static String sanitizeEndpointUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String clean = url.trim();
        // Disallow dangerous schemes or embedded credentials
        if (clean.contains("@")) {
            throw new IllegalArgumentException("Endpoint URL must not contain embedded user credentials");
        }
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            throw new IllegalArgumentException("Endpoint URL must use http:// or https:// protocol");
        }
        return clean;
    }

    public void updateMetadata(String name, String description, String endpointUrl, String configurationRef) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (endpointUrl != null) {
            this.endpointUrl = sanitizeEndpointUrl(endpointUrl);
        }
        if (configurationRef != null) {
            this.configurationRef = configurationRef.trim();
        }
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = DataSourceStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = DataSourceStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void markDegraded(String reason) {
        this.status = DataSourceStatus.DEGRADED;
        this.lastErrorAt = Instant.now();
        this.lastErrorMessage = sanitizeError(reason);
        this.updatedAt = Instant.now();
    }

    public void markNotConfigured() {
        this.status = DataSourceStatus.NOT_CONFIGURED;
        this.updatedAt = Instant.now();
    }

    public void recordSyncSuccess(int ingestedCount) {
        Instant now = Instant.now();
        this.lastSyncAt = now;
        this.lastSuccessAt = now;
        this.lastErrorMessage = null;
        this.totalIngestedCount += Math.max(0, ingestedCount);
        if (this.status == DataSourceStatus.ERROR || this.status == DataSourceStatus.DEGRADED) {
            this.status = DataSourceStatus.ACTIVE;
        }
        this.updatedAt = now;
    }

    public void recordSyncFailure(String errorMessage, int rejectedCount) {
        Instant now = Instant.now();
        this.lastSyncAt = now;
        this.lastErrorAt = now;
        this.lastErrorMessage = sanitizeError(errorMessage);
        this.totalRejectedCount += Math.max(0, rejectedCount);
        this.status = DataSourceStatus.ERROR;
        this.updatedAt = now;
    }

    public void incrementIngested(int count) {
        this.totalIngestedCount += Math.max(0, count);
        this.updatedAt = Instant.now();
    }

    public void incrementRejected(int count) {
        this.totalRejectedCount += Math.max(0, count);
        this.updatedAt = Instant.now();
    }

    private static String sanitizeError(String error) {
        if (error == null) return null;
        String clean = error.replaceAll("(?i)(password|token|secret|key|authorization)=[^&\\s]+", "$1=[REDACTED]");
        return clean.length() > 950 ? clean.substring(0, 950) + "..." : clean;
    }

    // Getters
    public UUID getId() { return id; }
    public String getSourceCode() { return sourceCode; }
    public String getName() { return name; }
    public DataSourceType getSourceType() { return sourceType; }
    public String getProviderName() { return providerName; }
    public String getDescription() { return description; }
    public DataSourceStatus getStatus() { return status; }
    public String getEndpointUrl() { return endpointUrl; }
    public String getConfigurationRef() { return configurationRef; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public Instant getLastSuccessAt() { return lastSuccessAt; }
    public Instant getLastErrorAt() { return lastErrorAt; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public long getTotalIngestedCount() { return totalIngestedCount; }
    public long getTotalRejectedCount() { return totalRejectedCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
