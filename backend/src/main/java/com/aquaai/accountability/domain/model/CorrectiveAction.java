package com.aquaai.accountability.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Traceable corrective action plan assigned to an operational owner.
 * Strictly non-actuating: no automatic gate, pump, valve, or physical control.
 */
@Getter
@Builder(toBuilder = true)
public class CorrectiveAction {

    private final UUID id;
    private final UUID incidentId;
    private final String actionNumber;
    private final String title;
    private final String description;
    private final String objective;
    private final UUID actionOwnerId;
    private final String actionOwnerName;
    private final String priority;
    private final Instant dueDate;
    private final String expectedOutcome;
    private ActionStatus status;
    private UUID approvedBy;
    private Instant approvedAt;
    private String approvalNotes;
    private UUID completedBy;
    private Instant completedAt;
    private String completionNotes;
    private final String verificationRequirement;
    private final Instant createdAt;
    private Instant updatedAt;

    public CorrectiveAction(
            UUID id,
            UUID incidentId,
            String actionNumber,
            String title,
            String description,
            String objective,
            UUID actionOwnerId,
            String actionOwnerName,
            String priority,
            Instant dueDate,
            String expectedOutcome,
            ActionStatus status,
            UUID approvedBy,
            Instant approvedAt,
            String approvalNotes,
            UUID completedBy,
            Instant completedAt,
            String completionNotes,
            String verificationRequirement,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.incidentId = Objects.requireNonNull(incidentId, "Incident ID cannot be null");
        this.actionNumber = actionNumber != null ? actionNumber : "ACT-" + System.currentTimeMillis() % 100000;
        this.title = Objects.requireNonNull(title, "Title cannot be null").trim();
        this.description = Objects.requireNonNull(description, "Description cannot be null").trim();
        this.objective = Objects.requireNonNull(objective, "Objective cannot be null").trim();
        this.actionOwnerId = actionOwnerId;
        this.actionOwnerName = actionOwnerName;
        this.priority = priority != null ? priority : "MEDIUM";
        this.dueDate = dueDate;
        this.expectedOutcome = Objects.requireNonNull(expectedOutcome, "Expected outcome cannot be null").trim();
        this.status = status != null ? status : ActionStatus.PROPOSED;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.approvalNotes = approvalNotes;
        this.completedBy = completedBy;
        this.completedAt = completedAt;
        this.completionNotes = completionNotes;
        this.verificationRequirement = verificationRequirement;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void approve(UUID approverId, String notes) {
        if (this.status != ActionStatus.PROPOSED) {
            throw new IllegalStateException("Only PROPOSED actions can be approved. Current status: " + this.status);
        }
        this.status = ActionStatus.APPROVED;
        this.approvedBy = Objects.requireNonNull(approverId, "Approver ID cannot be null");
        this.approvedAt = Instant.now();
        this.approvalNotes = notes;
        this.updatedAt = Instant.now();
    }

    public void start() {
        if (this.status != ActionStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED actions can be started. Current status: " + this.status);
        }
        this.status = ActionStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void complete(UUID completerId, String notes) {
        if (this.status != ActionStatus.IN_PROGRESS && this.status != ActionStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED or IN_PROGRESS actions can be completed. Current status: " + this.status);
        }
        this.status = ActionStatus.COMPLETED;
        this.completedBy = Objects.requireNonNull(completerId, "Completer ID cannot be null");
        this.completedAt = Instant.now();
        this.completionNotes = Objects.requireNonNull(notes, "Completion notes required").trim();
        this.updatedAt = Instant.now();
    }

    public void cancel(String notes) {
        if (this.status == ActionStatus.COMPLETED) {
            throw new IllegalStateException("Completed actions cannot be cancelled");
        }
        this.status = ActionStatus.CANCELLED;
        this.completionNotes = notes;
        this.updatedAt = Instant.now();
    }
}
