package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class GroundTruthMeasurement {
    private final UUID id;
    private final UUID pilotId;
    private final UUID fieldId;
    private final String measurementType;
    private final Double measuredValue;
    private final String canonicalUnit;
    private final Instant measuredAt;
    private final Instant submittedAt;
    private final String collectionMethod;
    private final String measurementSource;
    private final UUID submittedBy;
    private GroundTruthStatus verificationStatus;
    private UUID verifierId;
    private String evidenceReference;
    private String notes;
    private Instant verifiedAt;
    private String rejectionReason;

    public GroundTruthMeasurement(
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
            GroundTruthStatus verificationStatus,
            UUID verifierId,
            String evidenceReference,
            String notes,
            Instant verifiedAt,
            String rejectionReason) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.pilotId = Objects.requireNonNull(pilotId, "pilotId must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.measurementType = Objects.requireNonNull(measurementType, "measurementType must not be null");
        this.measuredValue = Objects.requireNonNull(measuredValue, "measuredValue must not be null");
        this.canonicalUnit = Objects.requireNonNull(canonicalUnit, "canonicalUnit must not be null");
        this.measuredAt = Objects.requireNonNull(measuredAt, "measuredAt must not be null");
        this.submittedAt = submittedAt != null ? submittedAt : Instant.now();
        this.collectionMethod = Objects.requireNonNull(collectionMethod, "collectionMethod must not be null");
        this.measurementSource = Objects.requireNonNull(measurementSource, "measurementSource must not be null");
        this.submittedBy = Objects.requireNonNull(submittedBy, "submittedBy must not be null");
        this.verificationStatus = verificationStatus != null ? verificationStatus : GroundTruthStatus.SUBMITTED;
        this.verifierId = verifierId;
        this.evidenceReference = evidenceReference;
        this.notes = notes;
        this.verifiedAt = verifiedAt;
        this.rejectionReason = rejectionReason;
    }

    public static GroundTruthMeasurement submit(
            UUID pilotId,
            UUID fieldId,
            String measurementType,
            Double measuredValue,
            String canonicalUnit,
            Instant measuredAt,
            String collectionMethod,
            String measurementSource,
            UUID submittedBy,
            String evidenceReference,
            String notes) {
        return new GroundTruthMeasurement(
                UUID.randomUUID(),
                pilotId,
                fieldId,
                measurementType,
                measuredValue,
                canonicalUnit,
                measuredAt,
                Instant.now(),
                collectionMethod,
                measurementSource,
                submittedBy,
                GroundTruthStatus.SUBMITTED,
                null,
                evidenceReference,
                notes,
                null,
                null
        );
    }

    /**
     * Four-eyes principle: The person submitting a measurement cannot verify their own measurement.
     */
    public void verify(UUID verifierId, String evidenceReference, String verificationNotes) {
        if (Objects.equals(this.submittedBy, verifierId)) {
            throw new IllegalArgumentException("Four-Eyes Principle Violation: Submitter cannot independently verify their own measurement");
        }
        this.verifierId = Objects.requireNonNull(verifierId, "verifierId must not be null");
        this.verificationStatus = GroundTruthStatus.VERIFIED;
        this.verifiedAt = Instant.now();
        if (evidenceReference != null && !evidenceReference.isBlank()) {
            this.evidenceReference = evidenceReference;
        }
        if (verificationNotes != null && !verificationNotes.isBlank()) {
            this.notes = (this.notes != null ? this.notes + " | Verification: " : "Verification: ") + verificationNotes;
        }
    }

    public void reject(UUID verifierId, String reason) {
        if (Objects.equals(this.submittedBy, verifierId)) {
            throw new IllegalArgumentException("Four-Eyes Principle Violation: Submitter cannot reject/review their own measurement");
        }
        this.verifierId = Objects.requireNonNull(verifierId, "verifierId must not be null");
        this.verificationStatus = GroundTruthStatus.REJECTED;
        this.rejectionReason = Objects.requireNonNull(reason, "Rejection reason must be provided");
        this.verifiedAt = Instant.now();
    }

    public void requestRemeasurement(UUID verifierId, String reason) {
        this.verifierId = Objects.requireNonNull(verifierId, "verifierId must not be null");
        this.verificationStatus = GroundTruthStatus.NEEDS_REMEASUREMENT;
        this.rejectionReason = reason;
        this.verifiedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getPilotId() { return pilotId; }
    public UUID getFieldId() { return fieldId; }
    public String getMeasurementType() { return measurementType; }
    public Double getMeasuredValue() { return measuredValue; }
    public String getCanonicalUnit() { return canonicalUnit; }
    public Instant getMeasuredAt() { return measuredAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getCollectionMethod() { return collectionMethod; }
    public String getMeasurementSource() { return measurementSource; }
    public UUID getSubmittedBy() { return submittedBy; }
    public GroundTruthStatus getVerificationStatus() { return verificationStatus; }
    public UUID getVerifierId() { return verifierId; }
    public String getEvidenceReference() { return evidenceReference; }
    public String getNotes() { return notes; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public String getRejectionReason() { return rejectionReason; }
}
