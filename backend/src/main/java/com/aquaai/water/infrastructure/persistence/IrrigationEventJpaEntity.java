package com.aquaai.water.infrastructure.persistence;

import com.aquaai.shared.domain.BaseAuditableEntity;
import com.aquaai.water.domain.model.IrrigationEventStatus;
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
 * JPA entity representing the "irrigation_events" table.
 */
@Getter
@Setter
@Entity
@Table(name = "irrigation_events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrrigationEventJpaEntity extends BaseAuditableEntity {

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "irrigation_plan_id")
    private UUID irrigationPlanId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "actual_volume_m3", precision = 14, scale = 4)
    private BigDecimal actualVolumeM3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private IrrigationEventStatus status = IrrigationEventStatus.STARTED;

    @Column(name = "notes", length = 500)
    private String notes;
}
