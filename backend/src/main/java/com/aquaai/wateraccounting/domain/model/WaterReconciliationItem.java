package com.aquaai.wateraccounting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Reconciliation audit record comparing upstream/expected water amounts with downstream/recorded amounts.
 */
@Getter
@Builder(toBuilder = true)
public class WaterReconciliationItem {

    private final UUID id;
    private final String boundaryType;
    private final String boundaryName;
    private final UUID targetId;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal expectedVolumeM3;
    private final BigDecimal recordedVolumeM3;
    private final BigDecimal discrepancyVolumeM3;
    private final BigDecimal discrepancyPercent;
    private final BigDecimal tolerancePercent;
    private ReconciliationStatus status;
    private final EvidenceClassification evidenceClassification;
    private final String explanation;
    private UUID reviewedBy;
    private Instant reviewedAt;
    private String reviewNotes;
    private final Instant createdAt;
    private Instant updatedAt;

    public WaterReconciliationItem(
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
            ReconciliationStatus status,
            EvidenceClassification evidenceClassification,
            String explanation,
            UUID reviewedBy,
            Instant reviewedAt,
            String reviewNotes,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.boundaryType = Objects.requireNonNull(boundaryType, "boundaryType must not be null");
        this.boundaryName = boundaryName != null ? boundaryName : "Boundary";
        this.targetId = targetId;
        this.periodStart = periodStart != null ? periodStart : Instant.now().minusSeconds(86400 * 30);
        this.periodEnd = periodEnd != null ? periodEnd : Instant.now();
        this.expectedVolumeM3 = expectedVolumeM3 != null ? expectedVolumeM3 : BigDecimal.ZERO;
        this.recordedVolumeM3 = recordedVolumeM3 != null ? recordedVolumeM3 : BigDecimal.ZERO;
        this.discrepancyVolumeM3 = discrepancyVolumeM3 != null ? discrepancyVolumeM3 : this.recordedVolumeM3.subtract(this.expectedVolumeM3);

        if (discrepancyPercent != null) {
            this.discrepancyPercent = discrepancyPercent;
        } else if (this.expectedVolumeM3.compareTo(BigDecimal.ZERO) > 0) {
            this.discrepancyPercent = this.discrepancyVolumeM3.abs().multiply(new BigDecimal("100.00"))
                    .divide(this.expectedVolumeM3, 2, RoundingMode.HALF_UP);
        } else {
            this.discrepancyPercent = BigDecimal.ZERO;
        }

        this.tolerancePercent = tolerancePercent != null ? tolerancePercent : new BigDecimal("5.00");
        this.status = status != null ? status : determineStatus(this.discrepancyPercent, this.tolerancePercent);
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.explanation = explanation;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNotes = reviewNotes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void review(UUID reviewerId, String notes) {
        this.reviewedBy = reviewerId;
        this.reviewedAt = Instant.now();
        this.reviewNotes = notes;
        this.updatedAt = Instant.now();
        if (this.status == ReconciliationStatus.DISCREPANCY) {
            this.status = ReconciliationStatus.WITHIN_TOLERANCE;
        }
    }

    private static ReconciliationStatus determineStatus(BigDecimal diffPercent, BigDecimal tolPercent) {
        if (diffPercent.compareTo(BigDecimal.ZERO) == 0) {
            return ReconciliationStatus.RECONCILED;
        } else if (diffPercent.compareTo(tolPercent) <= 0) {
            return ReconciliationStatus.WITHIN_TOLERANCE;
        } else {
            return ReconciliationStatus.DISCREPANCY;
        }
    }
}
