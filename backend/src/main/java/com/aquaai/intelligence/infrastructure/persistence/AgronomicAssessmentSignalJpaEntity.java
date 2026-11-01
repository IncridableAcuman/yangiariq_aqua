package com.aquaai.intelligence.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agronomic_assessment_signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgronomicAssessmentSignalJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private AgronomicAssessmentJpaEntity assessment;

    @Column(name = "signal_type", nullable = false, length = 64)
    private String signalType;

    @Column(name = "severity", nullable = false, length = 32)
    private String severity;

    @Column(name = "metric", nullable = false, length = 64)
    private String metric;

    @Column(name = "observed_value", precision = 14, scale = 4)
    private BigDecimal observedValue;

    @Column(name = "threshold_value", precision = 14, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
