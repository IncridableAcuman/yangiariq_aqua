package com.aquaai.integration.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Audit record detailing per-row validation status and reason for rejection during file imports.
 */
public class ImportRowResult {

    private final UUID id;
    private final UUID batchId;
    private final int rowNumber;
    private final boolean success;
    private final String externalRecordId;
    private final String errorMessage;
    private final Instant createdAt;

    public static ImportRowResult success(UUID batchId, int rowNumber, String externalRecordId) {
        return new ImportRowResult(UUID.randomUUID(), batchId, rowNumber, true, externalRecordId, null, Instant.now());
    }

    public static ImportRowResult failure(UUID batchId, int rowNumber, String externalRecordId, String errorMessage) {
        return new ImportRowResult(UUID.randomUUID(), batchId, rowNumber, false, externalRecordId, errorMessage, Instant.now());
    }

    public ImportRowResult(
            UUID id,
            UUID batchId,
            int rowNumber,
            boolean success,
            String externalRecordId,
            String errorMessage,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.batchId = Objects.requireNonNull(batchId, "Batch ID must not be null");
        this.rowNumber = rowNumber;
        this.success = success;
        this.externalRecordId = externalRecordId;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getBatchId() { return batchId; }
    public int getRowNumber() { return rowNumber; }
    public boolean isSuccess() { return success; }
    public String getExternalRecordId() { return externalRecordId; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
