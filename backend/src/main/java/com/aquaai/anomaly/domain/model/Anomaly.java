package com.aquaai.anomaly.domain.model;

import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Anomaly Aggregate Root representing an identified operational, hydraulic,
 * or environmental irregularity on an agricultural parcel in Yangiariq district.
 * <p>
 * Invariants & Lifecycle:
 * <ul>
 *   <li>Initial status is DETECTED.</li>
 *   <li>DETECTED -> ACKNOWLEDGED, INVESTIGATING, RESOLVED, DISMISSED.</li>
 *   <li>ACKNOWLEDGED -> INVESTIGATING, RESOLVED, DISMISSED.</li>
 *   <li>INVESTIGATING -> RESOLVED, DISMISSED.</li>
 *   <li>RESOLVED and DISMISSED are terminal states and cannot be silently reactivated.</li>
 * </ul>
 */
@Getter
public class Anomaly {

    private final UUID id;
    private final UUID fieldId;
    private final UUID sensorId;
    private final SensorType sensorType;
    private final AnomalyType anomalyType;
    private AnomalySeverity severity;
    private AnomalyStatus status;
    private BigDecimal detectedValue;
    private BigDecimal expectedValue;
    private BigDecimal deviation;
    private BigDecimal confidence;
    private final boolean isSimulated;
    private String description;
    private String explanation;
    private final String source;
    private int observationCount;
    private final Instant firstObservedAt;
    private Instant lastObservedAt;
    private final Instant detectedAt;
    private Instant acknowledgedAt;
    private UUID acknowledgedBy;
    private Instant resolvedAt;
    private UUID resolvedBy;
    private String resolutionNotes;
    private Instant lastNotifiedAt;
    private final List<AnomalyEvidence> evidence;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public Anomaly(
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
            Instant lastNotifiedAt,
            List<AnomalyEvidence> evidence,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.sensorId = sensorId;
        this.sensorType = Objects.requireNonNull(sensorType, "sensorType must not be null");
        this.anomalyType = Objects.requireNonNull(anomalyType, "anomalyType must not be null");
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.status = status != null ? status : AnomalyStatus.DETECTED;
        this.detectedValue = detectedValue;
        this.expectedValue = expectedValue;
        this.deviation = deviation;
        this.confidence = confidence != null ? confidence : BigDecimal.valueOf(0.80);
        this.isSimulated = isSimulated;
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.explanation = Objects.requireNonNull(explanation, "explanation must not be null");
        this.source = source != null ? source : "RULE_ENGINE";
        this.observationCount = Math.max(1, observationCount);
        this.firstObservedAt = firstObservedAt != null ? firstObservedAt : Instant.now();
        this.lastObservedAt = lastObservedAt != null ? lastObservedAt : this.firstObservedAt;
        this.detectedAt = detectedAt != null ? detectedAt : this.firstObservedAt;
        this.acknowledgedAt = acknowledgedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = resolutionNotes;
        this.lastNotifiedAt = lastNotifiedAt;
        this.evidence = evidence != null ? new ArrayList<>(evidence) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version != null ? version : 0L;
    }

    public static Anomaly create(
            UUID fieldId,
            UUID sensorId,
            SensorType sensorType,
            AnomalyCandidate candidate,
            Instant detectedAt
    ) {
        Instant now = detectedAt != null ? detectedAt : Instant.now();
        return new Anomaly(
                UUID.randomUUID(),
                fieldId,
                sensorId,
                sensorType,
                candidate.getAnomalyType(),
                candidate.getSeverity(),
                AnomalyStatus.DETECTED,
                candidate.getDetectedValue(),
                candidate.getExpectedValue(),
                candidate.getDeviation(),
                candidate.getConfidence(),
                candidate.isSimulated(),
                candidate.getDescription(),
                candidate.getExplanation(),
                candidate.getSource(),
                1,
                now,
                now,
                now,
                null,
                null,
                null,
                null,
                null,
                null,
                candidate.getEvidenceList(),
                now,
                now,
                0L
        );
    }

    /**
     * Records a repeated observation of an active anomaly.
     */
    public void recordRecurrence(
            BigDecimal newDetectedValue,
            String newExplanation,
            List<AnomalyEvidence> newEvidence,
            Instant observedAt
    ) {
        if (this.status.isTerminal()) {
            throw new BusinessRuleException(
                    "Cannot record recurrence for terminal anomaly with status " + this.status,
                    "ANOMALY_ALREADY_TERMINAL"
            );
        }
        this.observationCount++;
        this.lastObservedAt = observedAt != null ? observedAt : Instant.now();
        if (newDetectedValue != null) {
            this.detectedValue = newDetectedValue;
        }
        if (newExplanation != null && !newExplanation.isBlank()) {
            this.explanation = newExplanation;
        }
        if (newEvidence != null && !newEvidence.isEmpty()) {
            for (AnomalyEvidence e : newEvidence) {
                if (this.evidence.size() < 20) {
                    this.evidence.add(e);
                }
            }
        }
        this.updatedAt = this.lastObservedAt;
    }

    public void acknowledge(UUID userId, Instant at) {
        if (this.status != AnomalyStatus.DETECTED) {
            throw new BusinessRuleException(
                    "Cannot acknowledge anomaly with status " + this.status + "; only DETECTED anomalies can be acknowledged",
                    "INVALID_STATUS_TRANSITION"
            );
        }
        this.status = AnomalyStatus.ACKNOWLEDGED;
        this.acknowledgedBy = userId;
        this.acknowledgedAt = at != null ? at : Instant.now();
        this.updatedAt = this.acknowledgedAt;
    }

    public void investigate(UUID userId, Instant at) {
        if (this.status != AnomalyStatus.DETECTED && this.status != AnomalyStatus.ACKNOWLEDGED) {
            throw new BusinessRuleException(
                    "Cannot transition to INVESTIGATING from status " + this.status,
                    "INVALID_STATUS_TRANSITION"
            );
        }
        this.status = AnomalyStatus.INVESTIGATING;
        this.updatedAt = at != null ? at : Instant.now();
    }

    public void resolve(UUID userId, String notes, Instant at) {
        if (this.status.isTerminal()) {
            throw new BusinessRuleException(
                    "Anomaly is already in terminal status " + this.status,
                    "INVALID_STATUS_TRANSITION"
            );
        }
        this.status = AnomalyStatus.RESOLVED;
        this.resolvedBy = userId;
        this.resolvedAt = at != null ? at : Instant.now();
        this.resolutionNotes = notes;
        this.updatedAt = this.resolvedAt;
    }

    public void dismiss(UUID userId, String notes, Instant at) {
        if (this.status.isTerminal()) {
            throw new BusinessRuleException(
                    "Anomaly is already in terminal status " + this.status,
                    "INVALID_STATUS_TRANSITION"
            );
        }
        this.status = AnomalyStatus.DISMISSED;
        this.resolvedBy = userId;
        this.resolvedAt = at != null ? at : Instant.now();
        this.resolutionNotes = notes;
        this.updatedAt = this.resolvedAt;
    }

    public void markNotified(Instant at) {
        this.lastNotifiedAt = at != null ? at : Instant.now();
    }

    public List<AnomalyEvidence> getEvidence() {
        return Collections.unmodifiableList(evidence);
    }
}
