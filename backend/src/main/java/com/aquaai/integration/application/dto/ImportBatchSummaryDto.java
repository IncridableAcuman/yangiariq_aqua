package com.aquaai.integration.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ImportBatchSummaryDto(
        UUID batchId,
        String fileName,
        long fileSizeBytes,
        int totalRows,
        int successfulRows,
        int rejectedRows,
        String status,
        Instant startedAt,
        Instant completedAt,
        List<ImportRowResultDto> sampleErrors
) {}
