package com.aquaai.water.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * IrrigationPlan Aggregate Root representing scheduled irrigation for an agricultural parcel.
 * <p>
 * Invariants & State Transitions:
 * <ul>
 *   <li>Field ID must not be null.</li>
 *   <li>Planned volume must be strictly greater than zero m³.</li>
 *   <li>Planned start must be strictly before planned end.</li>
 *   <li>Method must not be null.</li>
 *   <li>Lifecycle transitions: PLANNED -> IN_PROGRESS -> COMPLETED, or (PLANNED / IN_PROGRESS) -> CANCELLED.</li>
 *   <li>Terminal states: COMPLETED and CANCELLED cannot transition further.</li>
 * </ul>
 */
@Getter
public class IrrigationPlan {

    public static final int MAX_NOTES_LENGTH = 500;

    private final UUID id;
    private final UUID fieldId;
    private UUID waterAllocationId;
    private Instant plannedStartAt;
    private Instant plannedEndAt;
    private BigDecimal plannedVolumeM3;
    private IrrigationMethod method;
    private IrrigationPlanStatus status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    /**
     * Domain factory method for creating a scheduled irrigation plan.
     */
    public static IrrigationPlan create(
            UUID fieldId,
            UUID waterAllocationId,
            Instant plannedStartAt,
            Instant plannedEndAt,
            BigDecimal plannedVolumeM3,
            IrrigationMethod method,
            String notes
    ) {
        return create(UUID.randomUUID(), fieldId, waterAllocationId, plannedStartAt, plannedEndAt, plannedVolumeM3, method, notes);
    }

    /**
     * Domain factory method with explicit ID.
     */
    public static IrrigationPlan create(
            UUID id,
            UUID fieldId,
            UUID waterAllocationId,
            Instant plannedStartAt,
            Instant plannedEndAt,
            BigDecimal plannedVolumeM3,
            IrrigationMethod method,
            String notes
    ) {
        Instant now = Instant.now();
        return new IrrigationPlan(
                id,
                fieldId,
                waterAllocationId,
                plannedStartAt,
                plannedEndAt,
                plannedVolumeM3,
                method,
                IrrigationPlanStatus.PLANNED,
                notes,
                now,
                now,
                null
        );
    }

    /**
     * Reconstitution constructor (used by repository/persistence mapper).
     */
    public IrrigationPlan(
            UUID id,
            UUID fieldId,
            UUID waterAllocationId,
            Instant plannedStartAt,
            Instant plannedEndAt,
            BigDecimal plannedVolumeM3,
            IrrigationMethod method,
            IrrigationPlanStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "IrrigationPlan ID must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.waterAllocationId = waterAllocationId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;

        setPlannedVolumeM3Internal(plannedVolumeM3);
        setScheduleInternal(plannedStartAt, plannedEndAt);
        setMethodInternal(method);
        setNotesInternal(notes);
        this.status = status != null ? status : IrrigationPlanStatus.PLANNED;
    }

    /**
     * Updates plan details while in PLANNED state.
     */
    public void updateDetails(
            UUID waterAllocationId,
            Instant plannedStartAt,
            Instant plannedEndAt,
            BigDecimal plannedVolumeM3,
            IrrigationMethod method,
            String notes
    ) {
        if (!isPlanned()) {
            throw new BusinessRuleException(
                    "Cannot update irrigation plan in status " + this.status + "; only PLANNED plans can be modified"
            );
        }
        this.waterAllocationId = waterAllocationId;
        setPlannedVolumeM3Internal(plannedVolumeM3);
        setScheduleInternal(plannedStartAt, plannedEndAt);
        setMethodInternal(method);
        setNotesInternal(notes);
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions plan from PLANNED to IN_PROGRESS.
     */
    public void start() {
        if (this.status != IrrigationPlanStatus.PLANNED) {
            throw new BusinessRuleException(
                    "Cannot start irrigation plan in status " + this.status + "; expected PLANNED"
            );
        }
        this.status = IrrigationPlanStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions plan from IN_PROGRESS to COMPLETED.
     */
    public void complete() {
        if (this.status != IrrigationPlanStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Cannot complete irrigation plan in status " + this.status + "; expected IN_PROGRESS"
            );
        }
        this.status = IrrigationPlanStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels an irrigation plan from PLANNED or IN_PROGRESS.
     */
    public void cancel(String reason) {
        if (this.status == IrrigationPlanStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot cancel an already COMPLETED irrigation plan");
        }
        if (this.status == IrrigationPlanStatus.CANCELLED) {
            throw new BusinessRuleException("Irrigation plan is already CANCELLED");
        }
        this.status = IrrigationPlanStatus.CANCELLED;
        if (reason != null && !reason.isBlank()) {
            appendNotes("Cancelled: " + reason.trim());
        }
        this.updatedAt = Instant.now();
    }

    public boolean isPlanned() {
        return this.status == IrrigationPlanStatus.PLANNED;
    }

    public boolean isInProgress() {
        return this.status == IrrigationPlanStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this.status == IrrigationPlanStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.status == IrrigationPlanStatus.CANCELLED;
    }

    private void setPlannedVolumeM3Internal(BigDecimal volume) {
        if (volume == null) {
            throw new BusinessRuleException("Planned volume must not be null");
        }
        if (volume.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Planned volume must be strictly greater than zero m³");
        }
        this.plannedVolumeM3 = volume;
    }

    private void setScheduleInternal(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new BusinessRuleException("Planned start and end timestamps must not be null");
        }
        if (!start.isBefore(end)) {
            throw new BusinessRuleException("Planned start timestamp must be strictly before planned end timestamp");
        }
        this.plannedStartAt = start;
        this.plannedEndAt = end;
    }

    private void setMethodInternal(IrrigationMethod method) {
        if (method == null) {
            throw new BusinessRuleException("Irrigation method must not be null");
        }
        this.method = method;
    }

    private void setNotesInternal(String notes) {
        if (notes != null) {
            String trimmed = notes.trim();
            if (trimmed.length() > MAX_NOTES_LENGTH) {
                throw new BusinessRuleException("Irrigation plan notes cannot exceed " + MAX_NOTES_LENGTH + " characters");
            }
            this.notes = trimmed.isEmpty() ? null : trimmed;
        } else {
            this.notes = null;
        }
    }

    private void appendNotes(String additional) {
        String trimmed = additional.trim();
        if (this.notes == null || this.notes.isBlank()) {
            setNotesInternal(trimmed);
        } else {
            setNotesInternal(this.notes + " | " + trimmed);
        }
    }
}
