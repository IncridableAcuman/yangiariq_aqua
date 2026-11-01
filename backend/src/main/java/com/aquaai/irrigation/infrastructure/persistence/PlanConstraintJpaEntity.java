package com.aquaai.irrigation.infrastructure.persistence;

import com.aquaai.irrigation.domain.model.ConstraintStatus;
import com.aquaai.irrigation.domain.model.ConstraintType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "smart_irrigation_plan_constraints")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanConstraintJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SmartIrrigationPlanJpaEntity plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "constraint_type", nullable = false, length = 64)
    private ConstraintType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ConstraintStatus status;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
