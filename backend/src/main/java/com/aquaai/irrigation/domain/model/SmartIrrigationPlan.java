package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing an AI-optimized, deterministic smart irrigation plan.
 * <p>
 * Enforces business invariants:
 * <ul>
 *   <li>Hard constraint precedence: Hydraulic safety > Hard water quotas > Infrastructure > Agronomy > Weather > AI explanation.</li>
 *   <li>Human-in-the-loop: Autonomous valve control is strictly prohibited; all plans require human approval.</li>
 *   <li>Immutable once completed or cancelled.</li>
 *   <li>Cancellation strictly requires an explanatory reason.</li>
 * </ul>
 */
@Getter
@Builder
public class SmartIrrigationPlan {

    public static final int MAX_REASON_LENGTH = 1000;
    public static final int MAX_CANCELLATION_REASON_LENGTH = 500;

    private final UUID id;
    private final UUID fieldId;
    private UUID waterAllocationId;
    private IrrigationPlanStatus status;
    private IrrigationPriority priority;
    private LocalDate plannedDate;
    private Instant plannedStartAt;
    private Instant plannedEndAt;
    private BigDecimal recommendedVolumeM3;
    private int estimatedDurationMinutes;
    private BigDecimal recommendedFlowRateM3H;
    private BigDecimal targetMoisturePercent;
    private BigDecimal currentMoisturePercent;
    private String confidenceLevel;
    private BigDecimal confidenceScore;
    private String reason;
    private String reasoningSummary;
    private String cancellationReason;
    private boolean isBlocked;
    private String blockedReason;
    private UUID approvedBy;
    private Instant approvedAt;
    private String source;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    @Builder.Default
    private List<PlanConstraint> constraints = new ArrayList<>();

    @Builder.Default
    private List<PlanTradeoff> tradeoffs = new ArrayList<>();

    public SmartIrrigationPlan(
            UUID id,
            UUID fieldId,
            UUID waterAllocationId,
            IrrigationPlanStatus status,
            IrrigationPriority priority,
            LocalDate plannedDate,
            Instant plannedStartAt,
            Instant plannedEndAt,
            BigDecimal recommendedVolumeM3,
            int estimatedDurationMinutes,
            BigDecimal recommendedFlowRateM3H,
            BigDecimal targetMoisturePercent,
            BigDecimal currentMoisturePercent,
            String confidenceLevel,
            BigDecimal confidenceScore,
            String reason,
            String reasoningSummary,
            String cancellationReason,
            boolean isBlocked,
            String blockedReason,
            UUID approvedBy,
            Instant approvedAt,
            String source,
            Instant createdAt,
            Instant updatedAt,
            Long version,
            List<PlanConstraint> constraints,
            List<PlanTradeoff> tradeoffs
    ) {
        this.id = Objects.requireNonNull(id, "Plan ID must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.waterAllocationId = waterAllocationId;
        this.status = status != null ? status : IrrigationPlanStatus.DRAFT;
        this.priority = priority != null ? priority : IrrigationPriority.MEDIUM;
        this.plannedDate = Objects.requireNonNull(plannedDate, "Planned date must not be null");
        setSchedule(plannedStartAt, plannedEndAt);
        setRecommendedVolume(recommendedVolumeM3);
        setEstimatedDuration(estimatedDurationMinutes);
        this.recommendedFlowRateM3H = recommendedFlowRateM3H != null ? recommendedFlowRateM3H.setScale(2, RoundingMode.HALF_UP) : null;
        this.targetMoisturePercent = targetMoisturePercent != null ? targetMoisturePercent.setScale(2, RoundingMode.HALF_UP) : null;
        this.currentMoisturePercent = currentMoisturePercent != null ? currentMoisturePercent.setScale(2, RoundingMode.HALF_UP) : null;
        this.confidenceLevel = confidenceLevel != null ? confidenceLevel : "MEDIUM";
        this.confidenceScore = confidenceScore != null ? confidenceScore.setScale(4, RoundingMode.HALF_UP) : BigDecimal.valueOf(0.70);
        this.reason = validateReason(reason);
        this.reasoningSummary = reasoningSummary;
        this.cancellationReason = cancellationReason;
        this.isBlocked = isBlocked;
        this.blockedReason = blockedReason;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.source = source != null ? source : "OPTIMIZER";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;
        this.constraints = constraints != null ? new ArrayList<>(constraints) : new ArrayList<>();
        this.tradeoffs = tradeoffs != null ? new ArrayList<>(tradeoffs) : new ArrayList<>();
    }

    public static SmartIrrigationPlan createDraft(
            UUID fieldId,
            UUID waterAllocationId,
            IrrigationPriority priority,
            LocalDate plannedDate,
            Instant plannedStartAt,
            Instant plannedEndAt,
            BigDecimal recommendedVolumeM3,
            int estimatedDurationMinutes,
            BigDecimal recommendedFlowRateM3H,
            BigDecimal targetMoisturePercent,
            BigDecimal currentMoisturePercent,
            String reason,
            String reasoningSummary,
            List<PlanConstraint> constraints,
            List<PlanTradeoff> tradeoffs
    ) {
        Instant now = Instant.now();
        return new SmartIrrigationPlan(
                UUID.randomUUID(),
                fieldId,
                waterAllocationId,
                IrrigationPlanStatus.DRAFT,
                priority,
                plannedDate,
                plannedStartAt,
                plannedEndAt,
                recommendedVolumeM3,
                estimatedDurationMinutes,
                recommendedFlowRateM3H,
                targetMoisturePercent,
                currentMoisturePercent,
                "HIGH",
                BigDecimal.valueOf(0.85),
                reason,
                reasoningSummary,
                null,
                false,
                null,
                null,
                null,
                "DETERMINISTIC_SCHEDULER",
                now,
                now,
                null,
                constraints,
                tradeoffs
        );
    }

    public void propose() {
        if (this.status != IrrigationPlanStatus.DRAFT) {
            throw new BusinessRuleException("Cannot propose irrigation plan in status " + this.status + "; expected DRAFT");
        }
        if (this.isBlocked) {
            throw new BusinessRuleException("Cannot propose blocked plan: " + this.blockedReason);
        }
        this.status = IrrigationPlanStatus.PROPOSED;
        this.updatedAt = Instant.now();
    }

    public void approve(UUID userId) {
        if (!this.status.canBeApproved()) {
            throw new BusinessRuleException("Cannot approve plan in status " + this.status + "; only DRAFT or PROPOSED plans can be approved");
        }
        if (this.isBlocked) {
            throw new BusinessRuleException("Cannot approve plan blocked by safety interlock: " + this.blockedReason);
        }
        this.status = IrrigationPlanStatus.APPROVED;
        this.approvedBy = Objects.requireNonNull(userId, "Approving user ID must not be null");
        this.approvedAt = Instant.now();
        this.updatedAt = this.approvedAt;
    }

    public void schedule() {
        if (this.status != IrrigationPlanStatus.APPROVED) {
            throw new BusinessRuleException("Cannot schedule plan in status " + this.status + "; expected APPROVED");
        }
        this.status = IrrigationPlanStatus.SCHEDULED;
        this.updatedAt = Instant.now();
    }

    public void start() {
        if (this.status != IrrigationPlanStatus.SCHEDULED && this.status != IrrigationPlanStatus.APPROVED) {
            throw new BusinessRuleException("Cannot start irrigation plan in status " + this.status + "; expected SCHEDULED or APPROVED");
        }
        this.status = IrrigationPlanStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void complete(BigDecimal actualVolumeDeliveredM3) {
        if (this.status != IrrigationPlanStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Cannot complete plan in status " + this.status + "; expected IN_PROGRESS");
        }
        this.status = IrrigationPlanStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status.isTerminal()) {
            throw new BusinessRuleException("Cannot cancel terminal irrigation plan in status " + this.status);
        }
        if (reason == null || reason.trim().isBlank()) {
            throw new BusinessRuleException("Cancellation reason must not be blank");
        }
        String trimmed = reason.trim();
        if (trimmed.length() > MAX_CANCELLATION_REASON_LENGTH) {
            throw new BusinessRuleException("Cancellation reason cannot exceed " + MAX_CANCELLATION_REASON_LENGTH + " characters");
        }
        this.cancellationReason = trimmed;
        this.status = IrrigationPlanStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void expire() {
        if (this.status.isTerminal()) {
            throw new BusinessRuleException("Cannot expire terminal plan in status " + this.status);
        }
        this.status = IrrigationPlanStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void block(String blockingReason) {
        this.isBlocked = true;
        this.blockedReason = blockingReason != null ? blockingReason.trim() : "Blocked by safety interlock";
        this.updatedAt = Instant.now();
    }

    public void unblock() {
        this.isBlocked = false;
        this.blockedReason = null;
        this.updatedAt = Instant.now();
    }

    public List<PlanConstraint> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    public List<PlanTradeoff> getTradeoffs() {
        return Collections.unmodifiableList(tradeoffs);
    }

    public void addConstraint(PlanConstraint constraint) {
        Objects.requireNonNull(constraint, "Constraint must not be null");
        boolean exists = this.constraints.stream().anyMatch(c -> c.getId().equals(constraint.getId()));
        if (!exists) {
            this.constraints.add(constraint);
            if (constraint.isBlocking()) {
                block(constraint.getMessage());
            }
        }
    }

    public void addTradeoff(PlanTradeoff tradeoff) {
        Objects.requireNonNull(tradeoff, "Tradeoff must not be null");
        boolean exists = this.tradeoffs.stream().anyMatch(t -> t.getId().equals(tradeoff.getId()));
        if (!exists) {
            this.tradeoffs.add(tradeoff);
        }
    }

    private void setSchedule(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new BusinessRuleException("Plan start and end timestamps must not be null");
        }
        if (!start.isBefore(end)) {
            throw new BusinessRuleException("Plan start must be strictly before plan end");
        }
        this.plannedStartAt = start;
        this.plannedEndAt = end;
    }

    private void setRecommendedVolume(BigDecimal volume) {
        if (volume == null) {
            throw new BusinessRuleException("Recommended volume must not be null");
        }
        if (volume.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Recommended volume cannot be negative");
        }
        this.recommendedVolumeM3 = volume.setScale(4, RoundingMode.HALF_UP);
    }

    private void setEstimatedDuration(int minutes) {
        if (minutes < 0) {
            throw new BusinessRuleException("Estimated duration minutes cannot be negative");
        }
        this.estimatedDurationMinutes = minutes;
    }

    private static String validateReason(String reason) {
        if (reason == null || reason.trim().isBlank()) {
            throw new BusinessRuleException("Plan reason must not be blank");
        }
        String trimmed = reason.trim();
        if (trimmed.length() > MAX_REASON_LENGTH) {
            return trimmed.substring(0, MAX_REASON_LENGTH);
        }
        return trimmed;
    }
}
