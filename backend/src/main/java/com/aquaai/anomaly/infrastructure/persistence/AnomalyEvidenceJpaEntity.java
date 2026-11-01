package com.aquaai.anomaly.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anomaly_evidence")
@Getter
@Setter
@NoArgsConstructor
public class AnomalyEvidenceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "anomaly_id", nullable = false)
    private UUID anomalyId;

    @Column(name = "evidence_type", nullable = false, length = 64)
    private String evidenceType;

    @Column(name = "metric", nullable = false, length = 64)
    private String metric;

    @Column(name = "observed_value", precision = 14, scale = 4)
    private BigDecimal observedValue;

    @Column(name = "expected_value", precision = 14, scale = 4)
    private BigDecimal expectedValue;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AnomalyEvidenceJpaEntity(
            UUID id,
            UUID anomalyId,
            String evidenceType,
            String metric,
            BigDecimal observedValue,
            BigDecimal expectedValue,
            Instant measuredAt,
            String metadata,
            Instant createdAt
    ) {
        this.id = id;
        this.anomalyId = anomalyId;
        this.evidenceType = evidenceType;
        this.metric = metric;
        this.observedValue = observedValue;
        this.expectedValue = expectedValue;
        this.measuredAt = measuredAt;
        this.metadata = metadata;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}
