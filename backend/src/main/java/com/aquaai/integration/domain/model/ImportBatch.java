package com.aquaai.integration.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a bulk batch file import execution.
 */
public class ImportBatch {

    private final UUID id;
    private final UUID dataSourceId;
    private final String fileName;
    private final long fileSizeBytes;
    private int totalRows;
    private int successfulRows;
    private int rejectedRows;
    private String status;
    private final UUID uploadedBy;
    private final Instant startedAt;
    private Instant completedAt;

    public static ImportBatch create(
            UUID dataSourceId,
            String fileName,
            long fileSizeBytes,
            UUID uploadedBy
    ) {
        return new ImportBatch(
                UUID.randomUUID(),
                dataSourceId,
                fileName,
                fileSizeBytes,
                0,
                0,
                0,
                "PENDING",
                uploadedBy,
                Instant.now(),
                null
        );
    }

    public ImportBatch(
            UUID id,
            UUID dataSourceId,
            String fileName,
            long fileSizeBytes,
            int totalRows,
            int successfulRows,
            int rejectedRows,
            String status,
            UUID uploadedBy,
            Instant startedAt,
            Instant completedAt
    ) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.dataSourceId = Objects.requireNonNull(dataSourceId, "Data source ID must not be null");
        this.fileName = Objects.requireNonNull(fileName, "File name must not be null");
        this.fileSizeBytes = fileSizeBytes;
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.rejectedRows = rejectedRows;
        this.status = status != null ? status : "PENDING";
        this.uploadedBy = uploadedBy;
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.completedAt = completedAt;
    }

    public void complete(int total, int success, int rejected) {
        this.totalRows = total;
        this.successfulRows = success;
        this.rejectedRows = rejected;
        this.completedAt = Instant.now();
        if (rejected == 0 && success > 0) {
            this.status = "COMPLETED";
        } else if (success > 0 && rejected > 0) {
            this.status = "PARTIAL";
        } else {
            this.status = "FAILED";
        }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getDataSourceId() { return dataSourceId; }
    public String getFileName() { return fileName; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public int getTotalRows() { return totalRows; }
    public int getSuccessfulRows() { return successfulRows; }
    public int getRejectedRows() { return rejectedRows; }
    public String getStatus() { return status; }
    public UUID getUploadedBy() { return uploadedBy; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
