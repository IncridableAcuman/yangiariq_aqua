package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class PilotProgram {
    private final UUID id;
    private final String code;
    private String name;
    private String description;
    private String geographicScope;
    private LocalDate startDate;
    private LocalDate endDate;
    private String organization;
    private final UUID ownerId;
    private String operationalObjectives;
    private PilotStatus status;
    private UUID approvedBy;
    private Instant approvedAt;
    private String approvalNotes;
    private final Instant createdAt;
    private Instant updatedAt;

    public PilotProgram(
            UUID id,
            String code,
            String name,
            String description,
            String geographicScope,
            LocalDate startDate,
            LocalDate endDate,
            String organization,
            UUID ownerId,
            String operationalObjectives,
            PilotStatus status,
            UUID approvedBy,
            Instant approvedAt,
            String approvalNotes,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.geographicScope = Objects.requireNonNull(geographicScope, "geographicScope must not be null");
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = Objects.requireNonNull(endDate, "endDate must not be null");
        this.organization = Objects.requireNonNull(organization, "organization must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.operationalObjectives = Objects.requireNonNull(operationalObjectives, "operationalObjectives must not be null");
        this.status = status != null ? status : PilotStatus.DRAFT;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.approvalNotes = approvalNotes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static PilotProgram create(
            String code,
            String name,
            String description,
            String geographicScope,
            LocalDate startDate,
            LocalDate endDate,
            String organization,
            UUID ownerId,
            String operationalObjectives) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Pilot end date cannot be before start date");
        }
        Instant now = Instant.now();
        return new PilotProgram(
                UUID.randomUUID(),
                code,
                name,
                description,
                geographicScope,
                startDate,
                endDate,
                organization,
                ownerId,
                operationalObjectives,
                PilotStatus.DRAFT,
                null,
                null,
                null,
                now,
                now
        );
    }

    public void update(
            String name,
            String description,
            String geographicScope,
            LocalDate startDate,
            LocalDate endDate,
            String organization,
            String operationalObjectives) {
        if (this.status == PilotStatus.COMPLETED || this.status == PilotStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update pilot program in terminal status: " + this.status);
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Pilot end date cannot be before start date");
        }
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.geographicScope = Objects.requireNonNull(geographicScope, "geographicScope must not be null");
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = Objects.requireNonNull(endDate, "endDate must not be null");
        this.organization = Objects.requireNonNull(organization, "organization must not be null");
        this.operationalObjectives = Objects.requireNonNull(operationalObjectives, "operationalObjectives must not be null");
        this.updatedAt = Instant.now();
    }

    public void approve(UUID approverId, String notes) {
        if (this.status != PilotStatus.DRAFT && this.status != PilotStatus.PLANNED) {
            throw new IllegalStateException("Only DRAFT or PLANNED pilots can be approved. Current: " + this.status);
        }
        this.approvedBy = Objects.requireNonNull(approverId, "approverId must not be null");
        this.approvedAt = Instant.now();
        this.approvalNotes = notes;
        this.status = PilotStatus.APPROVED;
        this.updatedAt = this.approvedAt;
    }

    public void markReady() {
        if (this.status != PilotStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED pilots can transition to READY. Current: " + this.status);
        }
        this.status = PilotStatus.READY;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (this.status != PilotStatus.READY && this.status != PilotStatus.APPROVED && this.status != PilotStatus.PAUSED) {
            throw new IllegalStateException("Cannot activate pilot in status: " + this.status + ". Must be APPROVED, READY, or PAUSED.");
        }
        this.status = PilotStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void pause() {
        if (this.status != PilotStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE pilots can be paused. Current: " + this.status);
        }
        this.status = PilotStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        if (this.status != PilotStatus.ACTIVE && this.status != PilotStatus.PAUSED) {
            throw new IllegalStateException("Only ACTIVE or PAUSED pilots can be completed. Current: " + this.status);
        }
        this.status = PilotStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status == PilotStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel an already COMPLETED pilot");
        }
        this.status = PilotStatus.CANCELLED;
        if (reason != null && !reason.isBlank()) {
            this.approvalNotes = (this.approvalNotes != null ? this.approvalNotes + " | Cancellation: " : "Cancellation: ") + reason;
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getGeographicScope() { return geographicScope; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getOrganization() { return organization; }
    public UUID getOwnerId() { return ownerId; }
    public String getOperationalObjectives() { return operationalObjectives; }
    public PilotStatus getStatus() { return status; }
    public UUID getApprovedBy() { return approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public String getApprovalNotes() { return approvalNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
