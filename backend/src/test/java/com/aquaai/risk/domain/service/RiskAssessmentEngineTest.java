package com.aquaai.risk.domain.service;

import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.model.TrendDirection;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentContext;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskEvidenceType;
import com.aquaai.risk.domain.model.RiskFactor;
import com.aquaai.risk.domain.model.RiskRecommendation;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.model.RiskTrend;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskAssessmentEngineTest {

    private RiskAssessmentEngine engine;
    private UUID fieldId;
    private UUID sensorId;
    private Instant now;

    @BeforeEach
    void setUp() {
        engine = new RiskAssessmentEngine();
        fieldId = UUID.randomUUID();
        sensorId = UUID.randomUUID();
        now = Instant.now();
    }

    private SensorReading createReading(SensorType type, BigDecimal value, Instant measuredAt, ReadingQuality quality) {
        MeasurementUnit unit = switch (type) {
            case SOIL_MOISTURE, HUMIDITY -> MeasurementUnit.PERCENT;
            case TEMPERATURE -> MeasurementUnit.CELSIUS;
            case PRESSURE -> MeasurementUnit.BAR;
            case WATER_FLOW -> MeasurementUnit.CUBIC_METER_PER_HOUR;
            default -> MeasurementUnit.PERCENT;
        };
        return SensorReading.create(sensorId, fieldId, type, value, unit, quality, "TEST", measuredAt);
    }

    @Test
    @DisplayName("1. Hydraulic safety precedence: Suspected pipe burst forces CRITICAL severity, Rank 1, and 100 score")
    void pipeBurstShouldEnforceHydraulicPrecedence() {
        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 101")
                .hasActiveWaterAllocation(true)
                .hasSuspectedPipeBurst(true)
                .hasCriticalPressureDrop(false)
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.isHydraulicBlocked()).isTrue();
        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.CRITICAL);
        assertThat(assessment.getPriorityRank()).isEqualTo(1);
        assertThat(assessment.getRiskScore()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(assessment.getHydraulicBlockReason()).contains("SUSPECTED_PIPE_BURST");
        assertThat(assessment.getRecommendations()).isNotEmpty();
        assertThat(assessment.getRecommendations().get(0).priorityRank()).isEqualTo(1);
        assertThat(assessment.getRecommendations().get(0).destinationRoute()).contains("/anomalies");
    }

    @Test
    @DisplayName("2. Hydraulic safety precedence: Critical pressure drop forces CRITICAL severity and Rank 1")
    void criticalPressureDropShouldEnforceHydraulicPrecedence() {
        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 102")
                .hasActiveWaterAllocation(true)
                .hasSuspectedPipeBurst(false)
                .hasCriticalPressureDrop(true)
                .currentPressureBar(new BigDecimal("0.30"))
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.isHydraulicBlocked()).isTrue();
        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.CRITICAL);
        assertThat(assessment.getPriorityRank()).isEqualTo(1);
        assertThat(assessment.getRiskScore()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(assessment.getHydraulicBlockReason()).contains("pressure drop");
    }

    @Test
    @DisplayName("3. Confirmed critical operational problem: Water quota exhausted while upcoming demands exist (Rank 2)")
    void waterQuotaExhaustedShouldEnforceRank2() {
        WaterBudget budget = WaterBudget.builder()
                .fieldId(fieldId)
                .totalAllocatedM3(new BigDecimal("1000.00"))
                .consumedM3(new BigDecimal("1000.00"))
                .committedM3(new BigDecimal("200.00"))
                .reservedM3(BigDecimal.ZERO)
                .remainingAvailableM3(BigDecimal.ZERO)
                .build();

        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("24.00"), now, ReadingQuality.VALID);

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 103")
                .hasActiveWaterAllocation(true)
                .waterBudget(budget)
                .totalUpcomingPlannedVolumeM3(new BigDecimal("200.00"))
                .latestReadings(List.of(moistureReading))
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.isHydraulicBlocked()).isFalse();
        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.CRITICAL);
        assertThat(assessment.getPriorityRank()).isEqualTo(2);
        assertThat(assessment.getPriorityReason()).contains("Rank 2");
        assertThat(assessment.getPrimaryRiskCategory()).isEqualTo(RiskCategory.WATER_AVAILABILITY);
    }

    @Test
    @DisplayName("4. High-confidence critical agricultural risk: Soil moisture < 15% with fresh telemetry (Rank 3)")
    void criticalSoilMoistureShouldEnforceRank3() {
        WaterBudget budget = WaterBudget.builder()
                .fieldId(fieldId)
                .totalAllocatedM3(new BigDecimal("2000.00"))
                .consumedM3(new BigDecimal("500.00"))
                .committedM3(BigDecimal.ZERO)
                .reservedM3(BigDecimal.ZERO)
                .remainingAvailableM3(new BigDecimal("1500.00"))
                .build();

        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("12.50"), now, ReadingQuality.VALID);
        Sensor activeSensor = Sensor.create(sensorId, fieldId, "Soil Probe 1", "SP-001", SensorType.SOIL_MOISTURE, "SIMULATED", "Probe");

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 104")
                .hasActiveWaterAllocation(true)
                .waterBudget(budget)
                .latestReadings(List.of(moistureReading))
                .fieldSensors(List.of(activeSensor))
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.CRITICAL);
        assertThat(assessment.getOverallConfidence()).isEqualTo(RiskConfidence.HIGH);
        assertThat(assessment.getPriorityRank()).isEqualTo(3);
        assertThat(assessment.getPriorityReason()).contains("Rank 3");
        assertThat(assessment.getPrimaryRiskCategory()).isEqualTo(RiskCategory.SOIL_MOISTURE);
    }

    @Test
    @DisplayName("5. Water shortage risk: Upcoming planned irrigation exceeds remaining budget (Rank 4)")
    void waterShortageShouldEnforceRank4() {
        WaterBudget budget = WaterBudget.builder()
                .fieldId(fieldId)
                .totalAllocatedM3(new BigDecimal("1000.00"))
                .consumedM3(new BigDecimal("800.00"))
                .committedM3(BigDecimal.ZERO)
                .reservedM3(BigDecimal.ZERO)
                .remainingAvailableM3(new BigDecimal("200.00"))
                .build();

        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("25.00"), now, ReadingQuality.VALID);

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 105")
                .hasActiveWaterAllocation(true)
                .waterBudget(budget)
                .totalUpcomingPlannedVolumeM3(new BigDecimal("350.00")) // Exceeds 200 remaining
                .latestReadings(List.of(moistureReading))
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(assessment.getPriorityRank()).isEqualTo(4);
        assertThat(assessment.getPriorityReason()).contains("Rank 4");
    }

    @Test
    @DisplayName("6. Severe soil moisture risk: Moisture < 20% with rapid drying trend (Rank 5)")
    void rapidMoistureDepletionShouldEnforceRank5() {
        WaterBudget budget = WaterBudget.builder()
                .fieldId(fieldId)
                .totalAllocatedM3(new BigDecimal("2000.00"))
                .consumedM3(new BigDecimal("400.00"))
                .committedM3(BigDecimal.ZERO)
                .reservedM3(BigDecimal.ZERO)
                .remainingAvailableM3(new BigDecimal("1600.00"))
                .build();

        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("18.00"), now, ReadingQuality.VALID);
        TelemetryTrend trend = new TelemetryTrend(
                TrendDirection.DECREASING,
                new BigDecimal("-0.2500"),
                10,
                now.minus(24, ChronoUnit.HOURS),
                now
        );

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 106")
                .hasActiveWaterAllocation(true)
                .waterBudget(budget)
                .latestReadings(List.of(moistureReading))
                .soilMoistureTrend(trend)
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(assessment.getPriorityRank()).isEqualTo(5);
        assertThat(assessment.getPriorityReason()).contains("Rank 5");
    }

    @Test
    @DisplayName("7. Epistemological distinction: Predicted deficit is explicitly labeled PREDICTED, not OBSERVED")
    void predictedDeficitShouldBeDistinguishedFromObserved() {
        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("24.00"), now, ReadingQuality.VALID);

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 107")
                .hasActiveWaterAllocation(true)
                .latestReadings(List.of(moistureReading))
                .predictedMoisture24h(13.2) // Predicted below 15% in 24h
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        boolean hasPredictedEvidence = assessment.getRiskFactors().stream()
                .flatMap(f -> f.evidenceList().stream())
                .anyMatch(e -> e.evidenceType() == RiskEvidenceType.PREDICTED && e.metricName().contains("PREDICTED_MOISTURE"));

        assertThat(hasPredictedEvidence).isTrue();
    }

    @Test
    @DisplayName("8. Missing data handling: Missing moisture telemetry is NOT interpreted as healthy condition")
    void missingMoistureTelemetryShouldBeFlaggedAsRiskWithInsufficientData() {
        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 108")
                .hasActiveWaterAllocation(true)
                .latestReadings(List.of()) // No readings
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.getOverallSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(assessment.getOverallConfidence()).isEqualTo(RiskConfidence.INSUFFICIENT_DATA);
        assertThat(assessment.getMissingDataWarnings()).isNotEmpty();
        assertThat(assessment.getMissingDataWarnings().get(0)).contains("soil moisture");
    }

    @Test
    @DisplayName("9. Stale telemetry handling: Sensor reading > 6h old lowers confidence tier")
    void staleTelemetryShouldLowerConfidence() {
        Instant staleTime = now.minus(8, ChronoUnit.HOURS);
        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("22.00"), staleTime, ReadingQuality.VALID);

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 109")
                .hasActiveWaterAllocation(true)
                .latestReadings(List.of(moistureReading))
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.getOverallConfidence()).isNotEqualTo(RiskConfidence.HIGH);
    }

    @Test
    @DisplayName("10. Degraded predictive model handling: Model degradation flags reliability risk factor")
    void degradedModelShouldBeIdentified() {
        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("24.00"), now, ReadingQuality.VALID);

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 110")
                .hasActiveWaterAllocation(true)
                .latestReadings(List.of(moistureReading))
                .predictionModelHealth(ModelHealthStatus.DEGRADED)
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        boolean hasModelFactor = assessment.getRiskFactors().stream()
                .anyMatch(f -> f.category() == RiskCategory.PREDICTIVE_MODEL_RELIABILITY && f.severity() == RiskSeverity.HIGH);

        assertThat(hasModelFactor).isTrue();
    }

    @Test
    @DisplayName("11. Trend evaluation: Score reduction of >= 5.0 reflects IMPROVING trend")
    void scoreReductionShouldReflectImprovingTrend() {
        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("26.00"), now, ReadingQuality.VALID);

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 111")
                .hasActiveWaterAllocation(true)
                .latestReadings(List.of(moistureReading))
                .previousRiskScore(new BigDecimal("75.00")) // Prior score was 75, now nominal
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.getRiskTrend()).isEqualTo(RiskTrend.IMPROVING);
    }

    @Test
    @DisplayName("12. Operational irrigation delay: Overdue approved plan flags operational risk (Rank 6)")
    void overduePlanShouldTriggerOperationalRisk() {
        SensorReading moistureReading = createReading(SensorType.SOIL_MOISTURE, new BigDecimal("25.00"), now, ReadingQuality.VALID);

        SmartIrrigationPlan overduePlan = SmartIrrigationPlan.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .status(IrrigationPlanStatus.APPROVED)
                .plannedDate(LocalDate.now().minusDays(2))
                .plannedStartAt(now.minus(48, ChronoUnit.HOURS))
                .plannedEndAt(now.minus(46, ChronoUnit.HOURS))
                .recommendedVolumeM3(new BigDecimal("150.00"))
                .reason("Scheduled hydration run")
                .build();

        RiskAssessmentContext ctx = RiskAssessmentContext.builder()
                .fieldId(fieldId)
                .fieldName("Parcel 112")
                .hasActiveWaterAllocation(true)
                .latestReadings(List.of(moistureReading))
                .activePlans(List.of(overduePlan))
                .evaluationTime(now)
                .build();

        FarmRiskAssessment assessment = engine.assess(ctx);

        assertThat(assessment.getPriorityRank()).isEqualTo(6);
        boolean hasOverdueFactor = assessment.getRiskFactors().stream()
                .anyMatch(f -> f.category() == RiskCategory.IRRIGATION_OPERATIONAL && f.title().contains("Overdue"));

        assertThat(hasOverdueFactor).isTrue();
    }

    @Test
    @DisplayName("13. Validation: Null context or null fieldId throws IllegalArgumentException")
    void nullContextShouldThrowException() {
        assertThatThrownBy(() -> engine.assess(null))
                .isInstanceOf(IllegalArgumentException.class);

        RiskAssessmentContext ctxWithoutField = RiskAssessmentContext.builder().build();
        assertThatThrownBy(() -> engine.assess(ctxWithoutField))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
