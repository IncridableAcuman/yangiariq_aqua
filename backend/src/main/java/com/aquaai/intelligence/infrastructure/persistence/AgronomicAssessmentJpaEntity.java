package com.aquaai.intelligence.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agronomic_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgronomicAssessmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "primary_signal", nullable = false, length = 64)
    private String primarySignal;

    @Column(name = "overall_severity", nullable = false, length = 32)
    private String overallSeverity;

    @Column(name = "soil_moisture_trend_direction", nullable = false, length = 32)
    private String soilMoistureTrendDirection;

    @Column(name = "soil_moisture_rate_per_hour", precision = 8, scale = 4)
    private BigDecimal soilMoistureRatePerHour;

    @Column(name = "telemetry_freshness", nullable = false, length = 32)
    private String telemetryFreshness;

    @Column(name = "confidence", nullable = false, length = 32)
    private String confidence;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "agronomic_summary", nullable = false, columnDefinition = "TEXT")
    private String agronomicSummary;

    @Column(name = "recommendations_overview", columnDefinition = "TEXT")
    private String recommendationsOverview;

    @Column(name = "hydraulic_safety_blocked", nullable = false)
    private boolean hydraulicSafetyBlocked;

    @Column(name = "hydraulic_safety_reason", length = 500)
    private String hydraulicSafetyReason;

    @Column(name = "water_budget_constrained", nullable = false)
    private boolean waterBudgetConstrained;

    @Column(name = "water_budget_reason", length = 500)
    private String waterBudgetReason;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AgronomicAssessmentSignalJpaEntity> signals = new ArrayList<>();

    public void addSignal(AgronomicAssessmentSignalJpaEntity signal) {
        if (signals == null) {
            signals = new ArrayList<>();
        }
        signals.add(signal);
        signal.setAssessment(this);
    }
}
