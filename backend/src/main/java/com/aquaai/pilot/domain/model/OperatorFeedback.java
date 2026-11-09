package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class OperatorFeedback {
    private final UUID id;
    private final UUID pilotId;
    private final UUID farmId;
    private final UUID fieldId;
    private final UUID sensorId;
    private final UUID dataSourceId;
    private final UUID incidentId;
    private final OperatorFeedbackCategory category;
    private String title;
    private String description;
    private OperatorFeedbackStatus status;
    private final UUID submittedBy;
    private UUID assignedTo;
    private String resolutionNotes;
    private final Instant createdAt;
    private Instant updatedAt;

    public OperatorFeedback(
            UUID id,
            UUID pilotId,
            UUID farmId,
            UUID fieldId,
            UUID sensorId,
            UUID dataSourceId,
            UUID incidentId,
            OperatorFeedbackCategory category,
            String title,
            String description,
            OperatorFeedbackStatus status,
            UUID submittedBy,
            UUID assignedTo,
            String resolutionNotes,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.pilotId = Objects.requireNonNull(pilotId, "pilotId must not be null");
        this.farmId = farmId;
        this.fieldId = fieldId;
        this.sensorId = sensorId;
        this.dataSourceId = dataSourceId;
        this.incidentId = incidentId;
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.status = status != null ? status : OperatorFeedbackStatus.SUBMITTED;
        this.submittedBy = Objects.requireNonNull(submittedBy, "submittedBy must not be null");
        this.assignedTo = assignedTo;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static OperatorFeedback submit(
            UUID pilotId,
            UUID farmId,
            UUID fieldId,
            UUID sensorId,
            UUID dataSourceId,
            UUID incidentId,
            OperatorFeedbackCategory category,
            String title,
            String description,
            UUID submittedBy) {
        Instant now = Instant.now();
        return new OperatorFeedback(
                UUID.randomUUID(),
                pilotId,
                farmId,
                fieldId,
                sensorId,
                dataSourceId,
                incidentId,
                category,
                title,
                description,
                OperatorFeedbackStatus.SUBMITTED,
                submittedBy,
                null,
                null,
                now,
                now
        );
    }

    public void updateStatus(OperatorFeedbackStatus newStatus, UUID assignedTo, String resolutionNotes) {
        this.status = Objects.requireNonNull(newStatus, "newStatus must not be null");
        if (assignedTo != null) {
            this.assignedTo = assignedTo;
        }
        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            this.resolutionNotes = resolutionNotes;
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getPilotId() { return pilotId; }
    public UUID getFarmId() { return farmId; }
    public UUID getFieldId() { return fieldId; }
    public UUID getSensorId() { return sensorId; }
    public UUID getDataSourceId() { return dataSourceId; }
    public UUID getIncidentId() { return incidentId; }
    public OperatorFeedbackCategory getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public OperatorFeedbackStatus getStatus() { return status; }
    public UUID getSubmittedBy() { return submittedBy; }
    public UUID getAssignedTo() { return assignedTo; }
    public String getResolutionNotes() { return resolutionNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
