package com.aquaai.pilot.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ground_truth_validations")
public class GroundTruthValidationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "pilot_id", nullable = false)
    private UUID pilotId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "measurement_type", nullable = false, length = 64)
    private String measurementType;

    @Column(name = "measured_value", nullable = false)
    private Double measuredValue;

    @Column(name = "canonical_unit", nullable = false, length = 32)
    private String canonicalUnit;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "collection_method", nullable = false, length = 128)
    private String collectionMethod;

    @Column(name = "measurement_source", nullable = false, length = 128)
    private String measurementSource;

    @Column(name = "submitted_by", nullable = false)
    private UUID submittedBy;

    @Column(name = "verification_status", nullable = false, length = 32)
    private String verificationStatus;

    @Column(name = "verifier_id")
    private UUID verifierId;

    @Column(name = "evidence_reference")
    private String evidenceReference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    public GroundTruthValidationEntity() {}

    public GroundTruthValidationEntity(
            UUID id,
            UUID pilotId,
            UUID fieldId,
            String measurementType,
            Double measuredValue,
            String canonicalUnit,
            Instant measuredAt,
            Instant submittedAt,
            String collectionMethod,
            String measurementSource,
            UUID submittedBy,
            String verificationStatus,
            UUID verifierId,
            String evidenceReference,
            String notes,
            Instant verifiedAt,
            String rejectionReason) {
        this.id = id;
        this.pilotId = pilotId;
        this.fieldId = fieldId;
        this.measurementType = measurementType;
        this.measuredValue = measuredValue;
        this.canonicalUnit = canonicalUnit;
        this.measuredAt = measuredAt;
        this.submittedAt = submittedAt;
        this.collectionMethod = collectionMethod;
        this.measurementSource = measurementSource;
        this.submittedBy = submittedBy;
        this.verificationStatus = verificationStatus;
        this.verifierId = verifierId;
        this.evidenceReference = evidenceReference;
        this.notes = notes;
        this.verifiedAt = verifiedAt;
        this.rejectionReason = rejectionReason;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPilotId() { return pilotId; }
    public void setPilotId(UUID pilotId) { this.pilotId = pilotId; }
    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }
    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }
    public Double getMeasuredValue() { return measuredValue; }
    public void setMeasuredValue(Double measuredValue) { this.measuredValue = measuredValue; }
    public String getCanonicalUnit() { return canonicalUnit; }
    public void setCanonicalUnit(String canonicalUnit) { this.canonicalUnit = canonicalUnit; }
    public Instant getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(Instant measuredAt) { this.measuredAt = measuredAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public String getCollectionMethod() { return collectionMethod; }
    public void setCollectionMethod(String collectionMethod) { this.collectionMethod = collectionMethod; }
    public String getMeasurementSource() { return measurementSource; }
    public void setMeasurementSource(String measurementSource) { this.measurementSource = measurementSource; }
    public UUID getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(UUID submittedBy) { this.submittedBy = submittedBy; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public UUID getVerifierId() { return verifierId; }
    public void setVerifierId(UUID verifierId) { this.verifierId = verifierId; }
    public String getEvidenceReference() { return evidenceReference; }
    public void setEvidenceReference(String evidenceReference) { this.evidenceReference = evidenceReference; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
