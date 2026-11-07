package com.aquaai.risk.application;

import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.risk.application.service.RiskAssessmentCoordinatorService;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.port.RiskAnomalyPort;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.port.RiskIrrigationPort;
import com.aquaai.risk.domain.port.RiskNotificationPort;
import com.aquaai.risk.domain.port.RiskPredictionPort;
import com.aquaai.risk.domain.port.RiskSensorPort;
import com.aquaai.risk.domain.port.RiskWaterPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentCoordinatorServiceTest {

    @Mock
    private RiskFieldPort fieldPort;

    @Mock
    private RiskWaterPort waterPort;

    @Mock
    private RiskSensorPort sensorPort;

    @Mock
    private RiskAnomalyPort anomalyPort;

    @Mock
    private RiskIrrigationPort irrigationPort;

    @Mock
    private RiskPredictionPort predictionPort;

    @Mock
    private RiskNotificationPort notificationPort;

    @Mock
    private RiskAssessmentRepository assessmentRepository;

    private RiskAssessmentCoordinatorService coordinatorService;
    private UUID fieldId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        coordinatorService = new RiskAssessmentCoordinatorService(
                fieldPort,
                waterPort,
                sensorPort,
                anomalyPort,
                irrigationPort,
                predictionPort,
                notificationPort,
                assessmentRepository
        );
        fieldId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("1. evaluateAndPersistFieldRisk persists assessment and sends notification on CRITICAL risk")
    void shouldEvaluatePersistAndNotifyOnCriticalRisk() {
        RiskFieldPort.FieldInfo fieldInfo = new RiskFieldPort.FieldInfo(
                fieldId, ownerId, "Parcel 1", new BigDecimal("12.5"), "COTTON", "SANDY_LOAM", "DRIP"
        );
        when(fieldPort.getFieldInfo(fieldId)).thenReturn(Optional.of(fieldInfo));
        when(assessmentRepository.findLatestByFieldId(fieldId)).thenReturn(Optional.empty());
        when(waterPort.getWaterBudget(fieldId)).thenReturn(Optional.empty());
        when(waterPort.hasActiveAllocation(fieldId)).thenReturn(true);
        when(waterPort.getTotalUpcomingPlannedVolumeM3(fieldId)).thenReturn(BigDecimal.ZERO);

        // Pipe burst triggers hydraulic safety block (CRITICAL)
        when(anomalyPort.hasSuspectedPipeBurst(fieldId)).thenReturn(true);
        when(anomalyPort.hasCriticalPressureDrop(fieldId)).thenReturn(false);

        when(sensorPort.getLatestReadings(fieldId)).thenReturn(Collections.emptyList());
        when(sensorPort.getMoistureTrend(fieldId)).thenReturn(Optional.empty());
        when(sensorPort.getSensors(fieldId)).thenReturn(Collections.emptyList());

        when(irrigationPort.getActivePlans(fieldId)).thenReturn(Collections.emptyList());
        when(irrigationPort.hasScheduleConflict(fieldId)).thenReturn(false);

        when(predictionPort.getPredictedSoilMoistureNext24h(fieldId)).thenReturn(null);
        when(predictionPort.getPredictedWaterDemandNext7d(fieldId)).thenReturn(null);
        when(predictionPort.getPredictionModelHealth()).thenReturn(ModelHealthStatus.HEALTHY);
        when(predictionPort.isPredictionModelDriftDetected()).thenReturn(false);
        when(predictionPort.getPredictionEvaluationSampleCount()).thenReturn(45);

        when(assessmentRepository.save(any(FarmRiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FarmRiskAssessment assessment = coordinatorService.evaluateAndPersistFieldRisk(fieldId);

        assertThat(assessment).isNotNull();
        assertThat(assessment.isHydraulicBlocked()).isTrue();
        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.CRITICAL);

        verify(assessmentRepository).save(any(FarmRiskAssessment.class));
        verify(notificationPort).sendRiskAlert(
                eq(assessment.getId()),
                eq(fieldId),
                eq("Parcel 1"),
                eq(RiskSeverity.CRITICAL),
                anyString(),
                anyString(),
                eq(false)
        );
    }

    @Test
    @DisplayName("2. evaluateAndPersistFieldRisk throws ResourceNotFoundException if field does not exist")
    void shouldThrowWhenFieldNotFound() {
        when(fieldPort.getFieldInfo(fieldId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coordinatorService.evaluateAndPersistFieldRisk(fieldId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(assessmentRepository, never()).save(any());
        verify(notificationPort, never()).sendRiskAlert(any(), any(), any(), any(), any(), any(), anyBoolean());
    }
}
