package com.aquaai.water.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * WaterAllocation Aggregate Root representing volumetric water quotas allocated to a specific
 * agricultural parcel from a water distribution source over a seasonal or defined time window.
 * <p>
 * Business Invariants:
 * <ul>
 *   <li>Field ID and Water Source ID must not be null.</li>
 *   <li>Allocated volume must be strictly greater than zero m³.</li>
 *   <li>Period start must be before or equal to period end.</li>
 *   <li>Status lifecycle transitions: ACTIVE -> CLOSED or CANCELLED. Closed/Cancelled are terminal.</li>
 * </ul>
 */
@Getter
public class WaterAllocation {

    public static final int MAX_NOTES_LENGTH = 500;

    private final UUID id;
    private final UUID fieldId;
    private final UUID waterSourceId;
    private BigDecimal allocatedVolumeM3;
    private Instant periodStart;
    private Instant periodEnd;
    private WaterAllocationStatus status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    /**
     * Domain factory method for creating a brand-new water allocation.
     */
    public static WaterAllocation create(
            UUID fieldId,
            UUID waterSourceId,
            BigDecimal allocatedVolumeM3,
            Instant periodStart,
            Instant periodEnd,
            String notes
    ) {
        return create(UUID.randomUUID(), fieldId, waterSourceId, allocatedVolumeM3, periodStart, periodEnd, notes);
    }

    /**
     * Domain factory method with explicit ID.
     */
    public static WaterAllocation create(
            UUID id,
            UUID fieldId,
            UUID waterSourceId,
            BigDecimal allocatedVolumeM3,
            Instant periodStart,
            Instant periodEnd,
            String notes
    ) {
        Instant now = Instant.now();
        return new WaterAllocation(
                id,
                fieldId,
                waterSourceId,
                allocatedVolumeM3,
                periodStart,
                periodEnd,
                WaterAllocationStatus.ACTIVE,
                notes,
                now,
                now,
                null
        );
    }

    /**
     * Reconstitution constructor (used by repository/persistence mapper).
     */
    public WaterAllocation(
            UUID id,
            UUID fieldId,
            UUID waterSourceId,
            BigDecimal allocatedVolumeM3,
            Instant periodStart,
            Instant periodEnd,
            WaterAllocationStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "WaterAllocation ID must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.waterSourceId = Objects.requireNonNull(waterSourceId, "WaterSource ID must not be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;

        setAllocatedVolumeM3Internal(allocatedVolumeM3);
        setPeriodInternal(periodStart, periodEnd);
        setNotesInternal(notes);
        this.status = status != null ? status : WaterAllocationStatus.ACTIVE;
    }

    /**
     * Updates allocation volume and window details for active allocations.
     */
    public void updateAllocation(
            BigDecimal allocatedVolumeM3,
            Instant periodStart,
            Instant periodEnd,
            String notes
    ) {
        if (!isActive()) {
            throw new BusinessRuleException(
                    "Cannot modify water allocation in status " + this.status + "; only ACTIVE allocations can be updated"
            );
        }
        setAllocatedVolumeM3Internal(allocatedVolumeM3);
        setPeriodInternal(periodStart, periodEnd);
        setNotesInternal(notes);
        this.updatedAt = Instant.now();
    }

    /**
     * Closes an active allocation upon natural expiration or season completion.
     */
    public void close(String reasonNotes) {
        if (!isActive()) {
            throw new BusinessRuleException(
                    "Cannot close allocation in status " + this.status + "; only ACTIVE allocations can be closed"
            );
        }
        this.status = WaterAllocationStatus.CLOSED;
        if (reasonNotes != null && !reasonNotes.isBlank()) {
            appendNotes(reasonNotes);
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels an active allocation due to schedule changes, operational cuts, or errors.
     */
    public void cancel(String reasonNotes) {
        if (!isActive()) {
            throw new BusinessRuleException(
                    "Cannot cancel allocation in status " + this.status + "; only ACTIVE allocations can be cancelled"
            );
        }
        this.status = WaterAllocationStatus.CANCELLED;
        if (reasonNotes != null && !reasonNotes.isBlank()) {
            appendNotes(reasonNotes);
        }
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == WaterAllocationStatus.ACTIVE;
    }

    public boolean isClosed() {
        return this.status == WaterAllocationStatus.CLOSED;
    }

    public boolean isCancelled() {
        return this.status == WaterAllocationStatus.CANCELLED;
    }

    public boolean isPeriodActiveAt(Instant target) {
        if (target == null || !isActive()) {
            return false;
        }
        return !target.isBefore(periodStart) && !target.isAfter(periodEnd);
    }

    private void setAllocatedVolumeM3Internal(BigDecimal volume) {
        if (volume == null) {
            throw new BusinessRuleException("Allocated volume must not be null");
        }
        if (volume.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Allocated volume must be strictly greater than zero m³");
        }
        this.allocatedVolumeM3 = volume;
    }

    private void setPeriodInternal(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new BusinessRuleException("Allocation period start and end timestamps must not be null");
        }
        if (start.isAfter(end)) {
            throw new BusinessRuleException("Allocation period start timestamp cannot be after period end timestamp");
        }
        this.periodStart = start;
        this.periodEnd = end;
    }

    private void setNotesInternal(String notes) {
        if (notes != null) {
            String trimmed = notes.trim();
            if (trimmed.length() > MAX_NOTES_LENGTH) {
                throw new BusinessRuleException("Allocation notes cannot exceed " + MAX_NOTES_LENGTH + " characters");
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
