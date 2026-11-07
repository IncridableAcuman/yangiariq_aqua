package com.aquaai.wateraccounting.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "water_reconciliations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterReconciliationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "boundary_type", nullable = false, length = 64)
    private String boundaryType;

    @Column(name = "boundary_name", nullable = false, length = 255)
    private String boundaryName;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "expected_volume_m3", nullable = false, precision = 14, scale = 4)
    private BigDecimal expectedVolumeM3;

    @Column(name = "recorded_volume_m3", nullable = false, precision = 14, scale = 4)
    private BigDecimal recordedVolumeM3;

    @Column(name = "discrepancy_volume_m3", nullable = false, precision = 14, scale = 4)
    private BigDecimal discrepancyVolumeM3;

    @Column(name = "discrepancy_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal discrepancyPercent;

    @Column(name = "tolerance_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal tolerancePercent;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "evidence_classification", nullable = false, length = 32)
    private String evidenceClassification;

    @Column(name = "explanation", length = 1000)
    private String explanation;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
