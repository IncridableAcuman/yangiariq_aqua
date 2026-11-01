package com.aquaai.irrigation.infrastructure.persistence;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "smart_irrigation_plans")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartIrrigationPlanJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "water_allocation_id")
    private UUID waterAllocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private IrrigationPlanStatus status = IrrigationPlanStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    @Builder.Default
    private IrrigationPriority priority = IrrigationPriority.MEDIUM;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "planned_start_at", nullable = false)
    private Instant plannedStartAt;

    @Column(name = "planned_end_at", nullable = false)
    private Instant plannedEndAt;

    @Column(name = "recommended_volume_m3", nullable = false, precision = 14, scale = 4)
    private BigDecimal recommendedVolumeM3;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private int estimatedDurationMinutes;

    @Column(name = "recommended_flow_rate_m3_h", precision = 10, scale = 2)
    private BigDecimal recommendedFlowRateM3H;

    @Column(name = "target_moisture_percent", precision = 5, scale = 2)
    private BigDecimal targetMoisturePercent;

    @Column(name = "current_moisture_percent", precision = 5, scale = 2)
    private BigDecimal currentMoisturePercent;

    @Column(name = "confidence_level", nullable = false, length = 32)
    @Builder.Default
    private String confidenceLevel = "MEDIUM";

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "reasoning_summary", columnDefinition = "TEXT")
    private String reasoningSummary;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private boolean isBlocked = false;

    @Column(name = "blocked_reason", length = 500)
    private String blockedReason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "source", nullable = false, length = 64)
    @Builder.Default
    private String source = "DETERMINISTIC_SCHEDULER";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanConstraintJpaEntity> constraints = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanTradeoffJpaEntity> tradeoffs = new ArrayList<>();
}
