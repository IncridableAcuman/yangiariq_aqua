package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WaterReconciliationResponse(
        UUID id,
        String boundaryType,
        String boundaryName,
        UUID targetId,
        Instant periodStart,
        Instant periodEnd,
        BigDecimal expectedVolumeM3,
        BigDecimal recordedVolumeM3,
        BigDecimal discrepancyVolumeM3,
        BigDecimal discrepancyPercent,
        BigDecimal tolerancePercent,
        String status,
        String evidenceClassification,
        String explanation,
        UUID reviewedBy,
        Instant reviewedAt,
        String reviewNotes,
        Instant createdAt,
        Instant updatedAt
) {}
