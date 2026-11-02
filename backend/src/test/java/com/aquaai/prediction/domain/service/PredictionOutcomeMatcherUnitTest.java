package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionClassificationResult;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.HydraulicAnomalyInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.IrrigationEventOutcome;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SensorHealthSummary;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SmartPlanOutcome;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionOutcomeMatcherUnitTest {

    @Mock
    private PredictionDataProvider dataProvider;

    private PredictionOutcomeMatcher matcher;
    private UUID fieldId;
    private Instant now;

    @BeforeEach
    void setUp() {
        matcher = new PredictionOutcomeMatcher();
        fieldId = UUID.randomUUID();
        now = Instant.now();
    }

    @Test
    @DisplayName("Target time in the future results in PENDING evaluation status")
    void futurePredictionIsPending() {
        Prediction futurePred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .targetTime(now.plus(12, ChronoUnit.HOURS))
                .generatedAt(now.minus(12, ChronoUnit.HOURS))
                .confidencePercent(85)
                .build();

        PredictionEvaluation eval = matcher.matchAndEvaluate(futurePred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.PENDING);
        assertThat(eval.getEvaluationMethod()).isEqualTo("PENDING_HORIZON_CLOSE");
    }

    @Test
    @DisplayName("Soil moisture matching finds nearest valid sensor reading within tolerance window")
    void soilMoistureEvaluationWithValidReading() {
        Instant target = now.minus(2, ChronoUnit.HOURS);
        Prediction pred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .targetTime(target)
                .generatedAt(now.minus(26, ChronoUnit.HOURS))
                .predictedValue(new BigDecimal("26.00"))
                .lowerBound(new BigDecimal("23.00"))
                .upperBound(new BigDecimal("29.00"))
                .confidencePercent(80)
                .build();

        TelemetryObservation reading = new TelemetryObservation(
                UUID.randomUUID(), "SOIL_MOISTURE", new BigDecimal("28.00"), "VALID", target.plus(15, ChronoUnit.MINUTES));

        when(dataProvider.getMoistureObservationsInWindow(eq(fieldId), any(), any()))
                .thenReturn(List.of(reading));

        PredictionEvaluation eval = matcher.matchAndEvaluate(pred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.EVALUATED);
        assertThat(eval.getActualValue()).isEqualByComparingTo(new BigDecimal("28.00"));
        assertThat(eval.getAbsoluteError()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(eval.getDirectionalError()).isEqualByComparingTo(new BigDecimal("-2.00")); // 26 - 28 = -2
        assertThat(eval.getIsWithinBounds()).isTrue();
        assertThat(eval.getDataQuality()).isEqualTo(EvaluationDataQuality.DATA_COMPLETE);
    }

    @Test
    @DisplayName("Soil moisture matching returns INSUFFICIENT_DATA when no sensor observations exist")
    void soilMoistureMissingObservations() {
        Instant target = now.minus(2, ChronoUnit.HOURS);
        Prediction pred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .targetTime(target)
                .generatedAt(now.minus(26, ChronoUnit.HOURS))
                .predictedValue(new BigDecimal("25.00"))
                .confidencePercent(75)
                .build();

        when(dataProvider.getMoistureObservationsInWindow(eq(fieldId), any(), any()))
                .thenReturn(Collections.emptyList());

        PredictionEvaluation eval = matcher.matchAndEvaluate(pred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.INSUFFICIENT_DATA);
        assertThat(eval.getFailureReason()).isEqualTo(EvaluationFailureReason.MISSING_SENSOR);
    }

    @Test
    @DisplayName("Irrigation need TRUE_POSITIVE when predicted LIKELY and qualifying event completed")
    void irrigationNeedTruePositive() {
        Instant generated = now.minus(30, ChronoUnit.HOURS);
        Instant target = now.minus(6, ChronoUnit.HOURS);

        Prediction pred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.IRRIGATION_NEED_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .generatedAt(generated)
                .targetTime(target)
                .likelihood(IrrigationLikelihood.LIKELY)
                .predictedValue(new BigDecimal("200.00"))
                .confidencePercent(88)
                .build();

        IrrigationEventOutcome event = new IrrigationEventOutcome(
                UUID.randomUUID(), fieldId, target.minus(2, ChronoUnit.HOURS), target, new BigDecimal("195.00"), "COMPLETED", "Executed");

        when(dataProvider.getIrrigationEventsInWindow(eq(fieldId), any(), any()))
                .thenReturn(List.of(event));
        when(dataProvider.getHydraulicAnomaliesInWindow(eq(fieldId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(dataProvider.getSmartPlansInWindow(eq(fieldId), any(), any()))
                .thenReturn(Collections.emptyList());

        PredictionEvaluation eval = matcher.matchAndEvaluate(pred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.EVALUATED);
        assertThat(eval.getClassificationResult()).isEqualTo(PredictionClassificationResult.TRUE_POSITIVE);
    }

    @Test
    @DisplayName("Irrigation need TRUE_NEGATIVE when predicted NOT_EXPECTED and no event occurred")
    void irrigationNeedTrueNegative() {
        Instant generated = now.minus(30, ChronoUnit.HOURS);
        Instant target = now.minus(6, ChronoUnit.HOURS);

        Prediction pred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.IRRIGATION_NEED_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .generatedAt(generated)
                .targetTime(target)
                .likelihood(IrrigationLikelihood.NOT_EXPECTED)
                .confidencePercent(90)
                .build();

        when(dataProvider.getIrrigationEventsInWindow(eq(fieldId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(dataProvider.getHydraulicAnomaliesInWindow(eq(fieldId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(dataProvider.getSmartPlansInWindow(eq(fieldId), any(), any()))
                .thenReturn(Collections.emptyList());

        PredictionEvaluation eval = matcher.matchAndEvaluate(pred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.EVALUATED);
        assertThat(eval.getClassificationResult()).isEqualTo(PredictionClassificationResult.TRUE_NEGATIVE);
    }

    @Test
    @DisplayName("Operational context: Predicted irrigation halted by hydraulic safety block is classified as OUTCOME_INTERRUPTED not FALSE_POSITIVE")
    void irrigationNeedHaltedBySafetyInterlock() {
        Instant generated = now.minus(30, ChronoUnit.HOURS);
        Instant target = now.minus(6, ChronoUnit.HOURS);

        Prediction pred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.IRRIGATION_NEED_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .generatedAt(generated)
                .targetTime(target)
                .likelihood(IrrigationLikelihood.LIKELY)
                .confidencePercent(85)
                .build();

        HydraulicAnomalyInfo criticalAnomaly = new HydraulicAnomalyInfo(
                UUID.randomUUID(), fieldId, "PRESSURE_SPIKE", "CRITICAL", target.minus(4, ChronoUnit.HOURS), false);

        SmartPlanOutcome blockedPlan = new SmartPlanOutcome(
                UUID.randomUUID(), fieldId, target.minus(2, ChronoUnit.HOURS), new BigDecimal("180.00"), "SCHEDULED", true, "Hydraulic safety block active");

        when(dataProvider.getIrrigationEventsInWindow(eq(fieldId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(dataProvider.getHydraulicAnomaliesInWindow(eq(fieldId), any(), any()))
                .thenReturn(List.of(criticalAnomaly));
        when(dataProvider.getSmartPlansInWindow(eq(fieldId), any(), any()))
                .thenReturn(List.of(blockedPlan));

        PredictionEvaluation eval = matcher.matchAndEvaluate(pred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.OUTCOME_INTERRUPTED);
        assertThat(eval.getClassificationResult()).isEqualTo(PredictionClassificationResult.INTERRUPTED);
        assertThat(eval.isSafetyInterlockTriggered()).isTrue();
        assertThat(eval.getFailureReason()).isEqualTo(EvaluationFailureReason.SAFETY_EVENT);
        assertThat(eval.getOperationalNotes()).contains("safety interlock");
    }

    @Test
    @DisplayName("Water demand evaluation matches actual consumed volume and records human decision context")
    void waterDemandEvaluation() {
        Instant generated = now.minus(48, ChronoUnit.HOURS);
        Instant target = now.minus(1, ChronoUnit.HOURS);

        Prediction pred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.WATER_DEMAND_FORECAST)
                .horizon(PredictionHorizon.NEXT_48_HOURS)
                .generatedAt(generated)
                .targetTime(target)
                .predictedValue(new BigDecimal("350.00"))
                .lowerBound(new BigDecimal("310.00"))
                .upperBound(new BigDecimal("390.00"))
                .confidencePercent(82)
                .build();

        when(dataProvider.getActualWaterConsumptionInWindow(eq(fieldId), any(), any()))
                .thenReturn(new BigDecimal("340.00"));

        SmartPlanOutcome plan = new SmartPlanOutcome(
                UUID.randomUUID(), fieldId, target.minus(10, ChronoUnit.HOURS), new BigDecimal("350.00"), "COMPLETED", false, null);

        when(dataProvider.getSmartPlansInWindow(eq(fieldId), any(), any()))
                .thenReturn(List.of(plan));

        PredictionEvaluation eval = matcher.matchAndEvaluate(pred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.EVALUATED);
        assertThat(eval.getActualValue()).isEqualByComparingTo(new BigDecimal("340.00"));
        assertThat(eval.getAbsoluteError()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(eval.getDirectionalError()).isEqualByComparingTo(new BigDecimal("10.00")); // 350 - 340 = +10
        assertThat(eval.getIsWithinBounds()).isTrue();
        assertThat(eval.getRecommendedValue()).isEqualByComparingTo(new BigDecimal("350.00"));
    }

    @Test
    @DisplayName("Sensor reliability forecast evaluates against telemetry failure and offline events")
    void sensorReliabilityEvaluation() {
        Instant generated = now.minus(7, ChronoUnit.DAYS);
        Instant target = now.minus(1, ChronoUnit.HOURS);

        Prediction pred = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(PredictionType.SENSOR_RELIABILITY_FORECAST)
                .horizon(PredictionHorizon.NEXT_7_DAYS)
                .generatedAt(generated)
                .targetTime(target)
                .riskLevel(RiskLevel.HIGH)
                .confidencePercent(78)
                .build();

        SensorHealthSummary offlineSensor = new SensorHealthSummary(
                UUID.randomUUID(), fieldId, "Moisture-101", "OFFLINE", now.minus(2, ChronoUnit.DAYS),
                100, 70, 10, 20, 2, 1);

        when(dataProvider.getSensorHealthHistory(eq(fieldId), any()))
                .thenReturn(List.of(offlineSensor));

        PredictionEvaluation eval = matcher.matchAndEvaluate(pred, dataProvider);

        assertThat(eval.getEvaluationStatus()).isEqualTo(EvaluationStatus.EVALUATED);
        assertThat(eval.getClassificationResult()).isEqualTo(PredictionClassificationResult.TRUE_POSITIVE);
    }
}
