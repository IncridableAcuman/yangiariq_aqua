package com.aquaai.water.infrastructure.persistence;

import com.aquaai.shared.domain.BaseAuditableEntity;
import com.aquaai.water.domain.model.IrrigationMethod;
import com.aquaai.water.domain.model.IrrigationPlanStatus;
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
 * JPA entity representing the "irrigation_plans" table.
 */
@Getter
@Setter
@Entity
@Table(name = "irrigation_plans")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrrigationPlanJpaEntity extends BaseAuditableEntity {

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "water_allocation_id")
    private UUID waterAllocationId;

    @Column(name = "planned_start_at", nullable = false)
    private Instant plannedStartAt;

    @Column(name = "planned_end_at", nullable = false)
    private Instant plannedEndAt;

    @Column(name = "planned_volume_m3", nullable = false, precision = 14, scale = 4)
    private BigDecimal plannedVolumeM3;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 32)
    private IrrigationMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private IrrigationPlanStatus status = IrrigationPlanStatus.PLANNED;

    @Column(name = "notes", length = 500)
    private String notes;
}
