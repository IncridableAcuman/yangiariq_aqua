package com.aquaai.integration.application.dto;

import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.domain.model.EvidenceClassification;
import com.aquaai.integration.domain.model.IngestedProvenance;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record IngestedProvenanceDto(
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
    public static IngestedProvenanceDto from(IngestedProvenance p) {
        if (p == null) return null;
        return new IngestedProvenanceDto(
                p.getId(),
                p.getDataSourceId(),
                p.getExternalRecordId(),
                p.getIdempotencyKey(),
                p.getSensorReadingId(),
                p.getFieldId(),
                p.getSensorId(),
                p.getSensorType(),
                p.getRawValue(),
                p.getRawUnit(),
                p.getNormalizedValue(),
                p.getCanonicalUnit(),
                p.getQualityStatus(),
                p.getEvidenceClassification(),
                p.getValidationMessage(),
                p.getMeasuredAt(),
                p.getIngestedAt()
        );
    }
}
