package com.aquaai.network.infrastructure.persistence;

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
@Table(name = "actual_dispatch_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualDispatchDeliveryJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dispatch_schedule_id")
    private UUID dispatchScheduleId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "edge_id")
    private UUID edgeId;

    @Column(name = "canal_name")
    private String canalName;

    @Column(name = "actual_start_at", nullable = false)
    private Instant actualStartAt;

    @Column(name = "actual_end_at", nullable = false)
    private Instant actualEndAt;

    @Column(name = "planned_volume_m3", precision = 12, scale = 2, nullable = false)
    private BigDecimal plannedVolumeM3;

    @Column(name = "actual_volume_m3", precision = 12, scale = 2, nullable = false)
    private BigDecimal actualVolumeM3;

    @Column(name = "variance_volume_m3", precision = 12, scale = 2, nullable = false)
    private BigDecimal varianceVolumeM3;

    @Column(name = "variance_percent", precision = 6, scale = 2, nullable = false)
    private BigDecimal variancePercent;

    @Column(name = "measured_flow_m3_s", precision = 8, scale = 4)
    private BigDecimal measuredFlowM3S;

    @Column(name = "operator_id")
    private UUID operatorId;

    @Column(name = "operator_notes", length = 1000)
    private String operatorNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
