package com.aquaai.pilot.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pilot_fields")
public class PilotFieldEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "pilot_id", nullable = false)
    private UUID pilotId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "monitoring_target", nullable = false)
    private String monitoringTarget;

    @Column(name = "measurement_plan", nullable = false, columnDefinition = "TEXT")
    private String measurementPlan;

    @Column(name = "validation_criteria", nullable = false, columnDefinition = "TEXT")
    private String validationCriteria;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    public PilotFieldEntity() {}

    public PilotFieldEntity(UUID id, UUID pilotId, UUID fieldId, String monitoringTarget, String measurementPlan, String validationCriteria, Instant addedAt) {
        this.id = id;
        this.pilotId = pilotId;
        this.fieldId = fieldId;
        this.monitoringTarget = monitoringTarget;
        this.measurementPlan = measurementPlan;
        this.validationCriteria = validationCriteria;
        this.addedAt = addedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPilotId() { return pilotId; }
    public void setPilotId(UUID pilotId) { this.pilotId = pilotId; }
    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }
    public String getMonitoringTarget() { return monitoringTarget; }
    public void setMonitoringTarget(String monitoringTarget) { this.monitoringTarget = monitoringTarget; }
    public String getMeasurementPlan() { return measurementPlan; }
    public void setMeasurementPlan(String measurementPlan) { this.measurementPlan = measurementPlan; }
    public String getValidationCriteria() { return validationCriteria; }
    public void setValidationCriteria(String validationCriteria) { this.validationCriteria = validationCriteria; }
    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
