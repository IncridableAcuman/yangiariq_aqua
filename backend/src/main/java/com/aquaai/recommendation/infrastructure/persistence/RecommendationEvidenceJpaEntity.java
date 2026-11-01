package com.aquaai.recommendation.infrastructure.persistence;

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
@Table(name = "recommendation_evidence")
@Getter
@Setter
@NoArgsConstructor
public class RecommendationEvidenceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "recommendation_id", nullable = false)
    private UUID recommendationId;

    @Column(name = "evidence_type", nullable = false, length = 64)
    private String evidenceType;

    @Column(name = "metric", nullable = false, length = 64)
    private String metric;

    @Column(name = "observed_value", precision = 14, scale = 4)
    private BigDecimal observedValue;

    @Column(name = "baseline_value", precision = 14, scale = 4)
    private BigDecimal baselineValue;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @Column(name = "source_reference", length = 128)
    private String sourceReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RecommendationEvidenceJpaEntity(
            UUID id,
            UUID recommendationId,
            String evidenceType,
            String metric,
            BigDecimal observedValue,
            BigDecimal baselineValue,
            String unit,
            String summary,
            String sourceReference,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.recommendationId = recommendationId;
        this.evidenceType = evidenceType;
        this.metric = metric;
        this.observedValue = observedValue;
        this.baselineValue = baselineValue;
        this.unit = unit;
        this.summary = summary;
        this.sourceReference = sourceReference;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}
