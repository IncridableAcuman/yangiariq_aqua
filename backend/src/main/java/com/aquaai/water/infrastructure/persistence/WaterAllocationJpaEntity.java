package com.aquaai.water.infrastructure.persistence;

import com.aquaai.shared.domain.BaseAuditableEntity;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing the "water_allocations" table.
 */
@Getter
@Setter
@Entity
@Table(name = "water_allocations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterAllocationJpaEntity extends BaseAuditableEntity {

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "water_source_id", nullable = false)
    private UUID waterSourceId;

    @Column(name = "allocated_volume_m3", nullable = false, precision = 14, scale = 4)
    private BigDecimal allocatedVolumeM3;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private WaterAllocationStatus status = WaterAllocationStatus.ACTIVE;

    @Column(name = "notes", length = 500)
    private String notes;
}
