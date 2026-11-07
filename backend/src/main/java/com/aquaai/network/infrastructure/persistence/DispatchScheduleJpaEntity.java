package com.aquaai.network.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "canal_dispatch_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchScheduleJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "target_wua_id")
    private UUID targetWuaId;

    @Column(name = "target_wua_name")
    private String targetWuaName;

    @Column(name = "total_requested_m3", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalRequestedM3;

    @Column(name = "total_allocated_m3", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAllocatedM3;

    @Column(name = "total_unmet_m3", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalUnmetM3;

    @Column(name = "assignments_json", nullable = false, columnDefinition = "TEXT")
    private String assignmentsJson;

    @Column(name = "conflicts_json", columnDefinition = "TEXT")
    private String conflictsJson;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(name = "fairness_policy_json", columnDefinition = "TEXT")
    private String fairnessPolicyJson;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
