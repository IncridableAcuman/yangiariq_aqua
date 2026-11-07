package com.aquaai.risk.domain.model;

import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Context input bundle passed into RiskAssessmentEngine for multi-domain risk evaluation.
 */
@Builder
public record RiskAssessmentContext(
        UUID fieldId,
        String fieldName,
        BigDecimal areaHectares,
        String cropType,
        String soilType,
        String irrigationMethod,
        WaterBudget waterBudget,
        boolean hasActiveWaterAllocation,
        BigDecimal totalUpcomingPlannedVolumeM3,
        List<SensorReading> latestReadings,
        TelemetryTrend soilMoistureTrend,
        boolean hasSuspectedPipeBurst,
        boolean hasCriticalPressureDrop,
        BigDecimal currentPressureBar,
        BigDecimal currentFlowM3H,
        List<SmartIrrigationPlan> activePlans,
        boolean hasScheduleConflict,
        List<Sensor> fieldSensors,
        Double predictedMoisture24h,
        Double predictedWaterDemandNext7d,
        ModelHealthStatus predictionModelHealth,
        boolean predictionModelDrift,
        int predictionSampleCount,
        Instant evaluationTime,
        BigDecimal previousRiskScore
) {
    public RiskAssessmentContext {
        latestReadings = latestReadings != null ? Collections.unmodifiableList(latestReadings) : Collections.emptyList();
        activePlans = activePlans != null ? Collections.unmodifiableList(activePlans) : Collections.emptyList();
        fieldSensors = fieldSensors != null ? Collections.unmodifiableList(fieldSensors) : Collections.emptyList();
        evaluationTime = evaluationTime != null ? evaluationTime : Instant.now();
    }
}
