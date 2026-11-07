package com.aquaai.network.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing an optimized, coordinated regional canal dispatch schedule.
 * Enforces human approval lifecycle: DRAFT -> PROPOSED -> APPROVED -> SCHEDULED -> IN_PROGRESS -> COMPLETED.
 */
@Getter
@Builder(toBuilder = true)
public class DispatchSchedule {

    private final UUID id;
    private final String title;
    private final Instant periodStart;
    private final Instant periodEnd;
    private DispatchStatus status;
    private final UUID targetWuaId;
    private final String targetWuaName;
    private BigDecimal totalRequestedM3;
    private BigDecimal totalAllocatedM3;
    private BigDecimal totalUnmetM3;
    private final List<DispatchAssignment> assignments;
    private final List<DispatchConflict> conflicts;
    private final Map<String, Object> metrics;
    private final FairnessPolicy fairnessPolicy;
    private String notes;
    private UUID approvedBy;
    private Instant approvedAt;
    private String rejectionReason;
    private final UUID createdBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public DispatchSchedule(
            UUID id,
            String title,
            Instant periodStart,
            Instant periodEnd,
            DispatchStatus status,
            UUID targetWuaId,
            String targetWuaName,
            BigDecimal totalRequestedM3,
            BigDecimal totalAllocatedM3,
            BigDecimal totalUnmetM3,
            List<DispatchAssignment> assignments,
            List<DispatchConflict> conflicts,
            Map<String, Object> metrics,
            FairnessPolicy fairnessPolicy,
            String notes,
            UUID approvedBy,
            Instant approvedAt,
            String rejectionReason,
            UUID createdBy,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.title = Objects.requireNonNull(title, "Schedule title must not be null");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart must not be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd must not be null");
        this.status = status != null ? status : DispatchStatus.DRAFT;
        this.targetWuaId = targetWuaId;
        this.targetWuaName = targetWuaName != null ? targetWuaName : "District Wide";
        this.totalRequestedM3 = totalRequestedM3 != null ? totalRequestedM3 : BigDecimal.ZERO;
        this.totalAllocatedM3 = totalAllocatedM3 != null ? totalAllocatedM3 : BigDecimal.ZERO;
        this.totalUnmetM3 = totalUnmetM3 != null ? totalUnmetM3 : BigDecimal.ZERO;
        this.assignments = assignments != null ? Collections.unmodifiableList(new ArrayList<>(assignments)) : Collections.emptyList();
        this.conflicts = conflicts != null ? Collections.unmodifiableList(new ArrayList<>(conflicts)) : Collections.emptyList();
        this.metrics = metrics != null ? Collections.unmodifiableMap(metrics) : Collections.emptyMap();
        this.fairnessPolicy = fairnessPolicy != null ? fairnessPolicy : FairnessPolicy.defaultPolicy();
        this.notes = notes;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.createdBy = createdBy;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;
    }

    public void approve(UUID userId, String approvalNotes) {
        if (this.status != DispatchStatus.PROPOSED && this.status != DispatchStatus.REVIEW_REQUIRED) {
            throw new IllegalStateException("Only PROPOSED or REVIEW_REQUIRED dispatch proposals can be approved. Current state: " + this.status);
        }
        this.status = DispatchStatus.APPROVED;
        this.approvedBy = Objects.requireNonNull(userId, "Approving userId must not be null");
        this.approvedAt = Instant.now();
        if (approvalNotes != null && !approvalNotes.isBlank()) {
            this.notes = (this.notes == null || this.notes.isBlank()) ? approvalNotes : this.notes + "\n[Approval Note]: " + approvalNotes;
        }
        this.updatedAt = Instant.now();
    }

    public void reject(UUID userId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        this.status = DispatchStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = Instant.now();
    }

    public void schedule() {
        this.status = DispatchStatus.SCHEDULED;
        this.updatedAt = Instant.now();
    }

    public void startExecution() {
        this.status = DispatchStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void completeExecution() {
        this.status = DispatchStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markBlocked(String reason) {
        this.status = DispatchStatus.BLOCKED;
        this.notes = (this.notes == null ? "" : this.notes + "\n") + "[BLOCKED]: " + reason;
        this.updatedAt = Instant.now();
    }
}
