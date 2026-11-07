package com.aquaai.risk.application.service;

import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentContext;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.port.RiskAnomalyPort;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.port.RiskIrrigationPort;
import com.aquaai.risk.domain.port.RiskNotificationPort;
import com.aquaai.risk.domain.port.RiskPredictionPort;
import com.aquaai.risk.domain.port.RiskSensorPort;
import com.aquaai.risk.domain.port.RiskWaterPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.risk.domain.service.RiskAssessmentEngine;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAssessmentCoordinatorService {

    private final RiskFieldPort fieldPort;
    private final RiskWaterPort waterPort;
    private final RiskSensorPort sensorPort;
    private final RiskAnomalyPort anomalyPort;
    private final RiskIrrigationPort irrigationPort;
    private final RiskPredictionPort predictionPort;
    private final RiskNotificationPort notificationPort;
    private final RiskAssessmentRepository assessmentRepository;
    private final RiskAssessmentEngine assessmentEngine = new RiskAssessmentEngine();

    @Transactional
    public FarmRiskAssessment evaluateAndPersistFieldRisk(UUID fieldId) {
        if (fieldId == null) {
            throw new IllegalArgumentException("fieldId must not be null");
        }

        RiskFieldPort.FieldInfo field = fieldPort.getFieldInfo(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        // Load historical previous assessment to assess trend
        Optional<FarmRiskAssessment> previous = assessmentRepository.findLatestByFieldId(fieldId);
        BigDecimal previousScore = previous.map(FarmRiskAssessment::getRiskScore).orElse(null);

        // Collect context from domain ports
        Optional<WaterBudget> budgetOpt = waterPort.getWaterBudget(fieldId);
        boolean hasAlloc = waterPort.hasActiveAllocation(fieldId);
        BigDecimal upcomingVol = waterPort.getTotalUpcomingPlannedVolumeM3(fieldId);

        boolean pipeBurst = anomalyPort.hasSuspectedPipeBurst(fieldId);
        boolean pressureDrop = anomalyPort.hasCriticalPressureDrop(fieldId);

        Double predMoisture = predictionPort.getPredictedSoilMoistureNext24h(fieldId);
        Double predWaterDemand = predictionPort.getPredictedWaterDemandNext7d(fieldId);
        ModelHealthStatus modelHealth = predictionPort.getPredictionModelHealth();
        boolean modelDrift = predictionPort.isPredictionModelDriftDetected();
        int sampleCount = predictionPort.getPredictionEvaluationSampleCount();

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName(field.name())
                .areaHectares(field.areaHectares())
                .cropType(field.cropType())
                .soilType(field.soilType())
                .irrigationMethod(field.irrigationMethod())
                .waterBudget(budgetOpt.orElse(null))
                .hasActiveWaterAllocation(hasAlloc)
                .totalUpcomingPlannedVolumeM3(upcomingVol)
                .latestReadings(sensorPort.getLatestReadings(fieldId))
                .soilMoistureTrend(sensorPort.getMoistureTrend(fieldId).orElse(null))
                .hasSuspectedPipeBurst(pipeBurst)
                .hasCriticalPressureDrop(pressureDrop)
                .currentPressureBar(null)
                .currentFlowM3H(null)
                .activePlans(irrigationPort.getActivePlans(fieldId))
                .hasScheduleConflict(irrigationPort.hasScheduleConflict(fieldId))
                .fieldSensors(sensorPort.getSensors(fieldId))
                .predictedMoisture24h(predMoisture)
                .predictedWaterDemandNext7d(predWaterDemand)
                .predictionModelHealth(modelHealth)
                .predictionModelDrift(modelDrift)
                .predictionSampleCount(sampleCount)
                .evaluationTime(Instant.now())
                .previousRiskScore(previousScore)
                .build();

        FarmRiskAssessment assessment = assessmentEngine.assess(ctx);
        FarmRiskAssessment saved = assessmentRepository.save(assessment);

        // Dispatch alert if critical or high
        if (saved.getOverallSeverity() == RiskSeverity.CRITICAL || saved.getOverallSeverity() == RiskSeverity.HIGH) {
            notificationPort.sendRiskAlert(
                    saved.getId(),
                    saved.getFieldId(),
                    saved.getFieldName(),
                    saved.getOverallSeverity(),
                    saved.getPriorityReason(),
                    "Risk score: " + saved.getRiskScore() + " (" + saved.getOverallSeverity() + ")",
                    false
            );
        }

        return saved;
    }
}
