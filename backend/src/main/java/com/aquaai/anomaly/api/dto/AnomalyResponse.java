package com.aquaai.anomaly.api.dto;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record AnomalyResponse(
        UUID id,
        UUID fieldId,
        UUID sensorId,
        SensorType sensorType,
        AnomalyType anomalyType,
        AnomalySeverity severity,
        AnomalyStatus status,
        BigDecimal detectedValue,
        BigDecimal expectedValue,
        BigDecimal deviation,
        BigDecimal confidence,
        boolean isSimulated,
        String description,
        String explanation,
        String source,
        int observationCount,
        Instant firstObservedAt,
        Instant lastObservedAt,
        Instant detectedAt,
        Instant acknowledgedAt,
        UUID acknowledgedBy,
        Instant resolvedAt,
        UUID resolvedBy,
        String resolutionNotes,
        List<AnomalyEvidenceResponse> evidence,
        Instant createdAt,
        Instant updatedAt
) {
    public static AnomalyResponse fromDomain(Anomaly domain) {
        if (domain == null) return null;
        List<AnomalyEvidenceResponse> evidenceResponses = domain.getEvidence() != null
                ? domain.getEvidence().stream().map(AnomalyEvidenceResponse::fromDomain).toList()
                : Collections.emptyList();

        return new AnomalyResponse(
                domain.getId(),
                domain.getFieldId(),
                domain.getSensorId(),
                domain.getSensorType(),
                domain.getAnomalyType(),
                domain.getSeverity(),
                domain.getStatus(),
                domain.getDetectedValue(),
                domain.getExpectedValue(),
                domain.getDeviation(),
                domain.getConfidence(),
                domain.isSimulated(),
                domain.getDescription(),
                domain.getExplanation(),
                domain.getSource(),
                domain.getObservationCount(),
                domain.getFirstObservedAt(),
                domain.getLastObservedAt(),
                domain.getDetectedAt(),
                domain.getAcknowledgedAt(),
                domain.getAcknowledgedBy(),
                domain.getResolvedAt(),
                domain.getResolvedBy(),
                domain.getResolutionNotes(),
                evidenceResponses,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
