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
@Table(name = "water_ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterLedgerEntryJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "entry_type", nullable = false, length = 64)
    private String entryType;

    @Column(name = "accounting_boundary", nullable = false, length = 64)
    private String accountingBoundary;

    @Column(name = "volume_m3", nullable = false, precision = 14, scale = 4)
    private BigDecimal volumeM3;

    @Column(name = "flow_rate_m3_s", precision = 8, scale = 4)
    private BigDecimal flowRateM3S;

    @Column(name = "source_node_or_canal", length = 255)
    private String sourceNodeOrCanal;

    @Column(name = "destination_node_or_field", length = 255)
    private String destinationNodeOrField;

    @Column(name = "field_id")
    private UUID fieldId;

    @Column(name = "wua_id")
    private UUID wuaId;

    @Column(name = "edge_id")
    private UUID edgeId;

    @Column(name = "dispatch_schedule_id")
    private UUID dispatchScheduleId;

    @Column(name = "allocation_id")
    private UUID allocationId;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "evidence_classification", nullable = false, length = 32)
    private String evidenceClassification;

    @Column(name = "data_source", nullable = false, length = 128)
    private String dataSource;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "recorded_by")
    private UUID recordedBy;

    @Column(name = "is_correction", nullable = false)
    private boolean isCorrection;

    @Column(name = "replaces_entry_id")
    private UUID replacesEntryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
