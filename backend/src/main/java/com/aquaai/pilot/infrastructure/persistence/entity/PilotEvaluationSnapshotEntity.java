package com.aquaai.pilot.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pilot_evaluation_snapshots")
public class PilotEvaluationSnapshotEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "pilot_id", nullable = false)
    private UUID pilotId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "data_performance_json", nullable = false, columnDefinition = "TEXT")
    private String dataPerformanceJson;

    @Column(name = "prediction_performance_json", nullable = false, columnDefinition = "TEXT")
    private String predictionPerformanceJson;

    @Column(name = "operational_performance_json", nullable = false, columnDefinition = "TEXT")
    private String operationalPerformanceJson;

    @Column(name = "water_outcomes_json", nullable = false, columnDefinition = "TEXT")
    private String waterOutcomesJson;

    @Column(name = "limitations_text", nullable = false, columnDefinition = "TEXT")
    private String limitationsText;

    @Column(name = "recommendations_text", nullable = false, columnDefinition = "TEXT")
    private String recommendationsText;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PilotEvaluationSnapshotEntity() {}

    public PilotEvaluationSnapshotEntity(
            UUID id,
            UUID pilotId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String dataPerformanceJson,
            String predictionPerformanceJson,
            String operationalPerformanceJson,
            String waterOutcomesJson,
            String limitationsText,
            String recommendationsText,
            UUID createdBy,
            Instant createdAt) {
        this.id = id;
        this.pilotId = pilotId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dataPerformanceJson = dataPerformanceJson;
        this.predictionPerformanceJson = predictionPerformanceJson;
        this.operationalPerformanceJson = operationalPerformanceJson;
        this.waterOutcomesJson = waterOutcomesJson;
        this.limitationsText = limitationsText;
        this.recommendationsText = recommendationsText;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPilotId() { return pilotId; }
    public void setPilotId(UUID pilotId) { this.pilotId = pilotId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getDataPerformanceJson() { return dataPerformanceJson; }
    public void setDataPerformanceJson(String dataPerformanceJson) { this.dataPerformanceJson = dataPerformanceJson; }
    public String getPredictionPerformanceJson() { return predictionPerformanceJson; }
    public void setPredictionPerformanceJson(String predictionPerformanceJson) { this.predictionPerformanceJson = predictionPerformanceJson; }
    public String getOperationalPerformanceJson() { return operationalPerformanceJson; }
    public void setOperationalPerformanceJson(String operationalPerformanceJson) { this.operationalPerformanceJson = operationalPerformanceJson; }
    public String getWaterOutcomesJson() { return waterOutcomesJson; }
    public void setWaterOutcomesJson(String waterOutcomesJson) { this.waterOutcomesJson = waterOutcomesJson; }
    public String getLimitationsText() { return limitationsText; }
    public void setLimitationsText(String limitationsText) { this.limitationsText = limitationsText; }
    public String getRecommendationsText() { return recommendationsText; }
    public void setRecommendationsText(String recommendationsText) { this.recommendationsText = recommendationsText; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
