package com.aquaai.wateraccounting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable ledger entry representing an auditable water movement or correction.
 */
@Getter
@Builder(toBuilder = true)
public class WaterLedgerEntry {

    private final UUID id;
    private final LedgerEntryType entryType;
    private final AccountingDimension accountingBoundary;
    private final BigDecimal volumeM3;
    private final BigDecimal flowRateM3S;
    private final String sourceNodeOrCanal;
    private final String destinationNodeOrField;
    private final UUID fieldId;
    private final UUID wuaId;
    private final UUID edgeId;
    private final UUID dispatchScheduleId;
    private final UUID allocationId;
    private final Instant eventTimestamp;
    private final EvidenceClassification evidenceClassification;
    private final String dataSource;
    private final String notes;
    private final UUID recordedBy;
    private final boolean isCorrection;
    private final UUID replacesEntryId;
    private final Instant createdAt;

    public WaterLedgerEntry(
            UUID id,
            LedgerEntryType entryType,
            AccountingDimension accountingBoundary,
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
            EvidenceClassification evidenceClassification,
            String dataSource,
            String notes,
            UUID recordedBy,
            boolean isCorrection,
            UUID replacesEntryId,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.entryType = Objects.requireNonNull(entryType, "entryType must not be null");
        this.accountingBoundary = Objects.requireNonNull(accountingBoundary, "accountingBoundary must not be null");
        this.volumeM3 = Objects.requireNonNull(volumeM3, "volumeM3 must not be null");
        this.flowRateM3S = flowRateM3S != null ? flowRateM3S : BigDecimal.ZERO;
        this.sourceNodeOrCanal = sourceNodeOrCanal != null ? sourceNodeOrCanal : "N/A";
        this.destinationNodeOrField = destinationNodeOrField != null ? destinationNodeOrField : "N/A";
        this.fieldId = fieldId;
        this.wuaId = wuaId;
        this.edgeId = edgeId;
        this.dispatchScheduleId = dispatchScheduleId;
        this.allocationId = allocationId;
        this.eventTimestamp = eventTimestamp != null ? eventTimestamp : Instant.now();
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.dataSource = dataSource != null ? dataSource : "SYSTEM";
        this.notes = notes;
        this.recordedBy = recordedBy;
        this.isCorrection = isCorrection;
        this.replacesEntryId = replacesEntryId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}
