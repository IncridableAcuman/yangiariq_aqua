package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PilotField {
    private final UUID id;
    private final UUID pilotId;
    private final UUID fieldId;
    private String monitoringTarget;
    private String measurementPlan;
    private String validationCriteria;
    private final Instant addedAt;

    public PilotField(
            UUID id,
            UUID pilotId,
            UUID fieldId,
            String monitoringTarget,
            String measurementPlan,
            String validationCriteria,
            Instant addedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.pilotId = Objects.requireNonNull(pilotId, "pilotId must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.monitoringTarget = Objects.requireNonNull(monitoringTarget, "monitoringTarget must not be null");
        this.measurementPlan = Objects.requireNonNull(measurementPlan, "measurementPlan must not be null");
        this.validationCriteria = Objects.requireNonNull(validationCriteria, "validationCriteria must not be null");
        this.addedAt = addedAt != null ? addedAt : Instant.now();
    }

    public static PilotField create(UUID pilotId, UUID fieldId, String monitoringTarget, String measurementPlan, String validationCriteria) {
        return new PilotField(UUID.randomUUID(), pilotId, fieldId, monitoringTarget, measurementPlan, validationCriteria, Instant.now());
    }

    public void update(String monitoringTarget, String measurementPlan, String validationCriteria) {
        this.monitoringTarget = Objects.requireNonNull(monitoringTarget, "monitoringTarget must not be null");
        this.measurementPlan = Objects.requireNonNull(measurementPlan, "measurementPlan must not be null");
        this.validationCriteria = Objects.requireNonNull(validationCriteria, "validationCriteria must not be null");
    }

    public UUID getId() { return id; }
    public UUID getPilotId() { return pilotId; }
    public UUID getFieldId() { return fieldId; }
    public String getMonitoringTarget() { return monitoringTarget; }
    public String getMeasurementPlan() { return measurementPlan; }
    public String getValidationCriteria() { return validationCriteria; }
    public Instant getAddedAt() { return addedAt; }
}
