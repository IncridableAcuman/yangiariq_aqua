package com.aquaai.water.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * IrrigationEvent Aggregate Root representing executed irrigation operations on an agricultural parcel.
 * <p>
 * Business Invariants & State Transitions:
 * <ul>
 *   <li>Field ID and startedAt must not be null.</li>
 *   <li>Status lifecycle: STARTED -> COMPLETED or CANCELLED.</li>
 *   <li>When COMPLETED, endedAt must not be null (endedAt > startedAt) and actualVolumeM3 must not be null (>= 0).</li>
 *   <li>COMPLETED and CANCELLED are terminal states.</li>
 * </ul>
 */
@Getter
public class IrrigationEvent {

    public static final int MAX_NOTES_LENGTH = 500;

    private final UUID id;
    private final UUID fieldId;
    private final UUID irrigationPlanId;
    private final Instant startedAt;
    private Instant endedAt;
    private BigDecimal actualVolumeM3;
    private IrrigationEventStatus status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    /**
     * Factory method for starting an in-progress irrigation operation in real-time.
     */
    public static IrrigationEvent start(UUID fieldId, UUID irrigationPlanId, Instant startedAt, String notes) {
        return start(UUID.randomUUID(), fieldId, irrigationPlanId, startedAt, notes);
    }

    /**
     * Factory method for starting an in-progress irrigation operation with explicit ID.
     */
    public static IrrigationEvent start(UUID id, UUID fieldId, UUID irrigationPlanId, Instant startedAt, String notes) {
        Instant now = Instant.now();
        Instant eventStart = startedAt != null ? startedAt : now;
        return new IrrigationEvent(
                id,
                fieldId,
                irrigationPlanId,
                eventStart,
                null,
                null,
                IrrigationEventStatus.STARTED,
                notes,
                now,
                now,
                null
        );
    }

    /**
     * Factory method for directly logging an already-completed historical irrigation event.
     */
    public static IrrigationEvent recordCompleted(
            UUID fieldId,
            UUID irrigationPlanId,
            Instant startedAt,
            Instant endedAt,
            BigDecimal actualVolumeM3,
            String notes
    ) {
        return recordCompleted(UUID.randomUUID(), fieldId, irrigationPlanId, startedAt, endedAt, actualVolumeM3, notes);
    }

    /**
     * Factory method for directly logging an already-completed historical irrigation event with explicit ID.
     */
    public static IrrigationEvent recordCompleted(
            UUID id,
            UUID fieldId,
            UUID irrigationPlanId,
            Instant startedAt,
            Instant endedAt,
            BigDecimal actualVolumeM3,
            String notes
    ) {
        Instant now = Instant.now();
        return new IrrigationEvent(
                id,
                fieldId,
                irrigationPlanId,
                startedAt,
                endedAt,
                actualVolumeM3,
                IrrigationEventStatus.COMPLETED,
                notes,
                now,
                now,
                null
        );
    }

    /**
     * Reconstitution constructor (used by repository/persistence mapper).
     */
    public IrrigationEvent(
            UUID id,
            UUID fieldId,
            UUID irrigationPlanId,
            Instant startedAt,
            Instant endedAt,
            BigDecimal actualVolumeM3,
            IrrigationEventStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "IrrigationEvent ID must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.irrigationPlanId = irrigationPlanId;
        this.startedAt = Objects.requireNonNull(startedAt, "Started at timestamp must not be null");
        this.status = status != null ? status : IrrigationEventStatus.STARTED;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;

        setNotesInternal(notes);

        if (this.status == IrrigationEventStatus.COMPLETED) {
            validateAndSetCompletionDetails(endedAt, actualVolumeM3);
        } else {
            this.endedAt = endedAt;
            this.actualVolumeM3 = actualVolumeM3;
            if (this.endedAt != null && !this.endedAt.isAfter(this.startedAt)) {
                throw new BusinessRuleException("Ended at timestamp must be strictly after started at timestamp");
            }
            if (this.actualVolumeM3 != null && this.actualVolumeM3.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException("Actual volume cannot be negative");
            }
        }
    }

    /**
     * Completes an active irrigation delivery event by recording finish time and consumed volume.
     */
    public void complete(Instant endedAt, BigDecimal actualVolumeM3, String completionNotes) {
        if (this.status != IrrigationEventStatus.STARTED) {
            throw new BusinessRuleException(
                    "Cannot complete irrigation event in status " + this.status + "; expected STARTED"
            );
        }
        validateAndSetCompletionDetails(endedAt, actualVolumeM3);
        this.status = IrrigationEventStatus.COMPLETED;
        if (completionNotes != null && !completionNotes.isBlank()) {
            appendNotes(completionNotes);
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels an in-progress irrigation delivery.
     */
    public void cancel(String reason) {
        if (this.status == IrrigationEventStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot cancel an already COMPLETED irrigation event");
        }
        if (this.status == IrrigationEventStatus.CANCELLED) {
            throw new BusinessRuleException("Irrigation event is already CANCELLED");
        }
        this.status = IrrigationEventStatus.CANCELLED;
        if (reason != null && !reason.isBlank()) {
            appendNotes("Cancelled: " + reason.trim());
        }
        this.updatedAt = Instant.now();
    }

    public boolean isStarted() {
        return this.status == IrrigationEventStatus.STARTED;
    }

    public boolean isCompleted() {
        return this.status == IrrigationEventStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.status == IrrigationEventStatus.CANCELLED;
    }

    /**
     * Computes water volume variance compared to planned volume: (plannedVolumeM3 - actualVolumeM3).
     * Positive indicates water conserved; negative indicates water over-applied.
     */
    public Optional<BigDecimal> calculateVarianceFromPlanned(BigDecimal plannedVolumeM3) {
        if (plannedVolumeM3 == null || this.actualVolumeM3 == null) {
            return Optional.empty();
        }
        return Optional.of(plannedVolumeM3.subtract(this.actualVolumeM3));
    }

    private void validateAndSetCompletionDetails(Instant endedAt, BigDecimal actualVolumeM3) {
        if (endedAt == null) {
            throw new BusinessRuleException("Ended at timestamp must not be null for completed irrigation");
        }
        if (!endedAt.isAfter(this.startedAt)) {
            throw new BusinessRuleException("Ended at timestamp must be strictly after started at timestamp");
        }
        if (actualVolumeM3 == null) {
            throw new BusinessRuleException("Actual volume must not be null for completed irrigation");
        }
        if (actualVolumeM3.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Actual volume must be greater than or equal to zero m³");
        }
        this.endedAt = endedAt;
        this.actualVolumeM3 = actualVolumeM3;
    }

    private void setNotesInternal(String notes) {
        if (notes != null) {
            String trimmed = notes.trim();
            if (trimmed.length() > MAX_NOTES_LENGTH) {
                throw new BusinessRuleException("Irrigation event notes cannot exceed " + MAX_NOTES_LENGTH + " characters");
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
