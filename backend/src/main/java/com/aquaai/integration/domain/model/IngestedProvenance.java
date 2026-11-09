package com.aquaai.integration.domain.model;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable audit record capturing the full provenance, quality validation, and lifecycle of ingested telemetry.
 */
public class IngestedProvenance {

    private final UUID id;
    private final UUID dataSourceId;
    private final String externalRecordId;
    private final String idempotencyKey;
    private final UUID sensorReadingId;
    private final UUID fieldId;
    private final UUID sensorId;
    private final SensorType sensorType;
    private final BigDecimal rawValue;
    private final String rawUnit;
    private final BigDecimal normalizedValue;
    private final MeasurementUnit canonicalUnit;
    private final DataQualityStatus qualityStatus;
    private final EvidenceClassification evidenceClassification;
    private final String validationMessage;
    private final Instant measuredAt;
    private final Instant ingestedAt;

    public static IngestedProvenance create(
            UUID dataSourceId,
            String externalRecordId,
            String idempotencyKey,
            UUID sensorReadingId,
            UUID fieldId,
            UUID sensorId,
            SensorType sensorType,
            BigDecimal rawValue,
            String rawUnit,
            BigDecimal normalizedValue,
            MeasurementUnit canonicalUnit,
            DataQualityStatus qualityStatus,
            EvidenceClassification evidenceClassification,
            String validationMessage,
            Instant measuredAt
    ) {
        return new IngestedProvenance(
                UUID.randomUUID(),
                dataSourceId,
                externalRecordId,
                idempotencyKey,
                sensorReadingId,
                fieldId,
                sensorId,
                sensorType,
                rawValue,
                rawUnit,
                normalizedValue,
                canonicalUnit,
                qualityStatus,
                evidenceClassification,
                validationMessage,
                measuredAt,
                Instant.now()
        );
    }

    public IngestedProvenance(
            UUID id,
            UUID dataSourceId,
            String externalRecordId,
            String idempotencyKey,
            UUID sensorReadingId,
            UUID fieldId,
            UUID sensorId,
            SensorType sensorType,
            BigDecimal rawValue,
            String rawUnit,
            BigDecimal normalizedValue,
            MeasurementUnit canonicalUnit,
            DataQualityStatus qualityStatus,
            EvidenceClassification evidenceClassification,
            String validationMessage,
            Instant measuredAt,
            Instant ingestedAt
    ) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.dataSourceId = Objects.requireNonNull(dataSourceId, "Data source ID must not be null");
        this.externalRecordId = externalRecordId;
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "Idempotency key must not be null");
        this.sensorReadingId = sensorReadingId;
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.sensorId = sensorId;
        this.sensorType = Objects.requireNonNull(sensorType, "Sensor type must not be null");
        this.rawValue = Objects.requireNonNull(rawValue, "Raw value must not be null");
        this.rawUnit = Objects.requireNonNull(rawUnit, "Raw unit must not be null");
        this.normalizedValue = Objects.requireNonNull(normalizedValue, "Normalized value must not be null");
        this.canonicalUnit = Objects.requireNonNull(canonicalUnit, "Canonical unit must not be null");
        this.qualityStatus = qualityStatus != null ? qualityStatus : DataQualityStatus.VALID;
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.OBSERVED;
        this.validationMessage = validationMessage;
        this.measuredAt = Objects.requireNonNull(measuredAt, "Measured at must not be null");
        this.ingestedAt = ingestedAt != null ? ingestedAt : Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getDataSourceId() { return dataSourceId; }
    public String getExternalRecordId() { return externalRecordId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getSensorReadingId() { return sensorReadingId; }
    public UUID getFieldId() { return fieldId; }
    public UUID getSensorId() { return sensorId; }
    public SensorType getSensorType() { return sensorType; }
    public BigDecimal getRawValue() { return rawValue; }
    public String getRawUnit() { return rawUnit; }
    public BigDecimal getNormalizedValue() { return normalizedValue; }
    public MeasurementUnit getCanonicalUnit() { return canonicalUnit; }
    public DataQualityStatus getQualityStatus() { return qualityStatus; }
    public EvidenceClassification getEvidenceClassification() { return evidenceClassification; }
    public String getValidationMessage() { return validationMessage; }
    public Instant getMeasuredAt() { return measuredAt; }
    public Instant getIngestedAt() { return ingestedAt; }
}
