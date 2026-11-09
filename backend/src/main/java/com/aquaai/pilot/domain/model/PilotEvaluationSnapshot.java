package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class PilotEvaluationSnapshot {
    private final UUID id;
    private final UUID pilotId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final String dataPerformanceJson;
    private final String predictionPerformanceJson;
    private final String operationalPerformanceJson;
    private final String waterOutcomesJson;
    private final String limitationsText;
    private final String recommendationsText;
    private final UUID createdBy;
    private final Instant createdAt;

    public PilotEvaluationSnapshot(
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
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.pilotId = Objects.requireNonNull(pilotId, "pilotId must not be null");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart must not be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd must not be null");
        this.dataPerformanceJson = Objects.requireNonNull(dataPerformanceJson, "dataPerformanceJson must not be null");
        this.predictionPerformanceJson = Objects.requireNonNull(predictionPerformanceJson, "predictionPerformanceJson must not be null");
        this.operationalPerformanceJson = Objects.requireNonNull(operationalPerformanceJson, "operationalPerformanceJson must not be null");
        this.waterOutcomesJson = Objects.requireNonNull(waterOutcomesJson, "waterOutcomesJson must not be null");
        this.limitationsText = Objects.requireNonNull(limitationsText, "limitationsText must not be null");
        this.recommendationsText = Objects.requireNonNull(recommendationsText, "recommendationsText must not be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static PilotEvaluationSnapshot create(
            UUID pilotId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String dataPerformanceJson,
            String predictionPerformanceJson,
            String operationalPerformanceJson,
            String waterOutcomesJson,
            String limitationsText,
            String recommendationsText,
            UUID createdBy) {
        return new PilotEvaluationSnapshot(
                UUID.randomUUID(),
                pilotId,
                periodStart,
                periodEnd,
                dataPerformanceJson,
                predictionPerformanceJson,
                operationalPerformanceJson,
                waterOutcomesJson,
                limitationsText,
                recommendationsText,
                createdBy,
                Instant.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getPilotId() { return pilotId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public String getDataPerformanceJson() { return dataPerformanceJson; }
    public String getPredictionPerformanceJson() { return predictionPerformanceJson; }
    public String getOperationalPerformanceJson() { return operationalPerformanceJson; }
    public String getWaterOutcomesJson() { return waterOutcomesJson; }
    public String getLimitationsText() { return limitationsText; }
    public String getRecommendationsText() { return recommendationsText; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
