package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WaterLedgerEntryResponse(
        UUID id,
        String entryType,
        String accountingBoundary,
        BigDecimal volumeM3,
        BigDecimal flowRateM3S,
        String sourceNodeOrCanal,
        String destinationNodeOrField,
        UUID fieldId,
        UUID wuaId,
        UUID edgeId,
        UUID dispatchScheduleId,
        UUID allocationId,
        Instant eventTimestamp,
        String evidenceClassification,
        String dataSource,
        String notes,
        UUID recordedBy,
        boolean isCorrection,
        UUID replacesEntryId,
        Instant createdAt
) {}
