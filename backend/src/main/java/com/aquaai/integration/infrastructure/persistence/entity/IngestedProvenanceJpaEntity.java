package com.aquaai.integration.infrastructure.persistence.entity;

import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.domain.model.EvidenceClassification;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ingested_provenance")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestedProvenanceJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "data_source_id", nullable = false, updatable = false)
    private UUID dataSourceId;

    @Column(name = "external_record_id", length = 128)
    private String externalRecordId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 160)
    private String idempotencyKey;

    @Column(name = "sensor_reading_id")
    private UUID sensorReadingId;

    @Column(name = "field_id", nullable = false, updatable = false)
    private UUID fieldId;

    @Column(name = "sensor_id")
    private UUID sensorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 32, updatable = false)
    private SensorType sensorType;

    @Column(name = "raw_value", nullable = false, precision = 14, scale = 4, updatable = false)
    private BigDecimal rawValue;

    @Column(name = "raw_unit", nullable = false, length = 32, updatable = false)
    private String rawUnit;

    @Column(name = "normalized_value", nullable = false, precision = 14, scale = 4, updatable = false)
    private BigDecimal normalizedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "canonical_unit", nullable = false, length = 32, updatable = false)
    private MeasurementUnit canonicalUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status", nullable = false, length = 32, updatable = false)
    private DataQualityStatus qualityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_classification", nullable = false, length = 32, updatable = false)
    private EvidenceClassification evidenceClassification;

    @Column(name = "validation_message", length = 500)
    private String validationMessage;

    @Column(name = "measured_at", nullable = false, updatable = false)
    private Instant measuredAt;

    @Column(name = "ingested_at", nullable = false, updatable = false)
    private Instant ingestedAt;
}
