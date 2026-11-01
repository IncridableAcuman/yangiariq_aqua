package com.aquaai.intelligence.domain.model;

import com.aquaai.intelligence.domain.service.AgronomicAssessmentEngine;
import com.aquaai.intelligence.domain.service.FreshnessEvaluator;
import com.aquaai.intelligence.domain.service.IntelligenceRecommendationEngine;
import com.aquaai.intelligence.domain.service.TemporalTrendCalculator;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntelligenceDomainUnitTest {

    private final FreshnessEvaluator freshnessEvaluator = new FreshnessEvaluator();
    private final TemporalTrendCalculator trendCalculator = new TemporalTrendCalculator();
    private final AgronomicAssessmentEngine assessmentEngine = new AgronomicAssessmentEngine();
    private final IntelligenceRecommendationEngine recommendationEngine = new IntelligenceRecommendationEngine();

    @Test
    @DisplayName("TelemetrySignal: authoritative and disqualified invariants")
    void telemetrySignalInvariants() {
        UUID fieldId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant now = Instant.now();

        TelemetrySignal validFresh = new TelemetrySignal(
                fieldId, sensorId, SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(18.5), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.FRESH
        );
        assertThat(validFresh.isAuthoritative()).isTrue();
        assertThat(validFresh.isSuspectOrStale()).isFalse();
        assertThat(validFresh.isDisqualified()).isFalse();

        TelemetrySignal suspectSignal = new TelemetrySignal(
                fieldId, sensorId, SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(18.5), MeasurementUnit.PERCENT,
                ReadingQuality.SUSPECT, now, TelemetryFreshness.FRESH
        );
        assertThat(suspectSignal.isAuthoritative()).isFalse();
        assertThat(suspectSignal.isSuspectOrStale()).isTrue();

        TelemetrySignal invalidSignal = new TelemetrySignal(
                fieldId, sensorId, SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(18.5), MeasurementUnit.PERCENT,
                ReadingQuality.INVALID, now, TelemetryFreshness.FRESH
        );
        assertThat(invalidSignal.isAuthoritative()).isFalse();
        assertThat(invalidSignal.isDisqualified()).isTrue();

        TelemetrySignal missingSignal = new TelemetrySignal(
                fieldId, sensorId, SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(18.5), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.MISSING
        );
        assertThat(missingSignal.isDisqualified()).isTrue();
    }

    @Test
    @DisplayName("FreshnessEvaluator: evaluates SLA bounds across sensor modalities")
    void freshnessEvaluationRules() {
        Instant now = Instant.now();

        // Soil moisture: <= 6h fresh, <= 24h stale, > 24h missing
        assertThat(freshnessEvaluator.evaluate(SensorType.SOIL_MOISTURE, now.minus(3, ChronoUnit.HOURS), now))
                .isEqualTo(TelemetryFreshness.FRESH);
        assertThat(freshnessEvaluator.evaluate(SensorType.SOIL_MOISTURE, now.minus(12, ChronoUnit.HOURS), now))
                .isEqualTo(TelemetryFreshness.STALE);
        assertThat(freshnessEvaluator.evaluate(SensorType.SOIL_MOISTURE, now.minus(30, ChronoUnit.HOURS), now))
                .isEqualTo(TelemetryFreshness.MISSING);
        assertThat(freshnessEvaluator.evaluate(SensorType.SOIL_MOISTURE, null, now))
                .isEqualTo(TelemetryFreshness.MISSING);

        // Temperature: <= 2h fresh, <= 12h stale, > 12h missing
        assertThat(freshnessEvaluator.evaluate(SensorType.TEMPERATURE, now.minus(1, ChronoUnit.HOURS), now))
                .isEqualTo(TelemetryFreshness.FRESH);
        assertThat(freshnessEvaluator.evaluate(SensorType.TEMPERATURE, now.minus(4, ChronoUnit.HOURS), now))
                .isEqualTo(TelemetryFreshness.STALE);
        assertThat(freshnessEvaluator.evaluate(SensorType.TEMPERATURE, now.minus(15, ChronoUnit.HOURS), now))
                .isEqualTo(TelemetryFreshness.MISSING);

        // Pressure: <= 30min fresh, <= 2h stale, > 2h missing
        assertThat(freshnessEvaluator.evaluate(SensorType.PRESSURE, now.minus(15, ChronoUnit.MINUTES), now))
                .isEqualTo(TelemetryFreshness.FRESH);
        assertThat(freshnessEvaluator.evaluate(SensorType.PRESSURE, now.minus(60, ChronoUnit.MINUTES), now))
                .isEqualTo(TelemetryFreshness.STALE);
        assertThat(freshnessEvaluator.evaluate(SensorType.PRESSURE, now.minus(180, ChronoUnit.MINUTES), now))
                .isEqualTo(TelemetryFreshness.MISSING);
    }

    @Test
    @DisplayName("TemporalTrendCalculator: detects decreasing, increasing, and stable moisture trends")
    void temporalTrendCalculation() {
        Instant now = Instant.now();

        // 1. Decreasing trend
        List<TemporalTrendCalculator.DataPoint> dryingPoints = List.of(
                new TemporalTrendCalculator.DataPoint(new BigDecimal("22.0"), now.minus(4, ChronoUnit.HOURS)),
                new TemporalTrendCalculator.DataPoint(new BigDecimal("20.5"), now.minus(2, ChronoUnit.HOURS)),
                new TemporalTrendCalculator.DataPoint(new BigDecimal("19.0"), now)
        );
        TelemetryTrend trendDown = trendCalculator.calculateTrend(dryingPoints);
        assertThat(trendDown.getDirection()).isEqualTo(TrendDirection.DECREASING);
        assertThat(trendDown.getRateOfChangePerHour()).isLessThan(BigDecimal.ZERO);
        assertThat(trendDown.getDataPointsEvaluated()).isEqualTo(3);

        // 2. Increasing trend
        List<TemporalTrendCalculator.DataPoint> wettingPoints = List.of(
                new TemporalTrendCalculator.DataPoint(new BigDecimal("15.0"), now.minus(2, ChronoUnit.HOURS)),
                new TemporalTrendCalculator.DataPoint(new BigDecimal("21.0"), now)
        );
        TelemetryTrend trendUp = trendCalculator.calculateTrend(wettingPoints);
        assertThat(trendUp.getDirection()).isEqualTo(TrendDirection.INCREASING);
        assertThat(trendUp.getRateOfChangePerHour()).isGreaterThan(BigDecimal.ZERO);

        // 3. Stable trend
        List<TemporalTrendCalculator.DataPoint> stablePoints = List.of(
                new TemporalTrendCalculator.DataPoint(new BigDecimal("20.0"), now.minus(2, ChronoUnit.HOURS)),
                new TemporalTrendCalculator.DataPoint(new BigDecimal("20.1"), now)
        );
        TelemetryTrend trendStable = trendCalculator.calculateTrend(stablePoints);
        assertThat(trendStable.getDirection()).isEqualTo(TrendDirection.STABLE);

        // 4. Insufficient history
        TelemetryTrend trendEmpty = trendCalculator.calculateTrend(List.of());
        assertThat(trendEmpty.getDirection()).isEqualTo(TrendDirection.STABLE);
        assertThat(trendEmpty.getDataPointsEvaluated()).isEqualTo(0);
    }

    @Test
    @DisplayName("AgronomicAssessmentEngine: detects critical and moderate soil moisture deficit")
    void soilMoistureDeficitEvaluation() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();

        // Critical deficit (<15%)
        TelemetrySignal criticalMoisture = new TelemetrySignal(
                fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(13.5), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.FRESH
        );
        AgronomicAssessmentEngine.AssessmentInput inputCrit = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(criticalMoisture), TelemetryTrend.stable(1, now, now),
                false, false, BigDecimal.valueOf(500), now
        );
        AgronomicAssessment assessmentCrit = assessmentEngine.assess(inputCrit);

        assertThat(assessmentCrit.getPrimarySignal()).isEqualTo(AgronomicSignalType.SOIL_TOO_DRY);
        assertThat(assessmentCrit.getOverallSeverity()).isEqualTo(IntelligenceSeverity.CRITICAL);
        assertThat(assessmentCrit.getConfidence()).isEqualTo(IntelligenceConfidence.HIGH);

        // Moderate deficit (15%-20%)
        TelemetrySignal modMoisture = new TelemetrySignal(
                fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(17.5), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.FRESH
        );
        AgronomicAssessmentEngine.AssessmentInput inputMod = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(modMoisture), TelemetryTrend.stable(1, now, now),
                false, false, BigDecimal.valueOf(500), now
        );
        AgronomicAssessment assessmentMod = assessmentEngine.assess(inputMod);

        assertThat(assessmentMod.getPrimarySignal()).isEqualTo(AgronomicSignalType.SOIL_TOO_DRY);
        assertThat(assessmentMod.getOverallSeverity()).isEqualTo(IntelligenceSeverity.HIGH);
    }

    @Test
    @DisplayName("AgronomicAssessmentEngine: detects soil saturation")
    void soilSaturationEvaluation() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();

        TelemetrySignal wetMoisture = new TelemetrySignal(
                fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(38.0), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.FRESH
        );
        AgronomicAssessmentEngine.AssessmentInput inputWet = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(wetMoisture), TelemetryTrend.stable(1, now, now),
                false, false, BigDecimal.valueOf(500), now
        );
        AgronomicAssessment assessmentWet = assessmentEngine.assess(inputWet);

        assertThat(assessmentWet.getPrimarySignal()).isEqualTo(AgronomicSignalType.SOIL_TOO_WET);
        assertThat(assessmentWet.getOverallSeverity()).isEqualTo(IntelligenceSeverity.MEDIUM);
    }

    @Test
    @DisplayName("AgronomicAssessmentEngine: hydraulic safety interlock overrides agronomic signals")
    void hydraulicSafetyOverridesAgronomicSignals() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();

        TelemetrySignal dryMoisture = new TelemetrySignal(
                fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(11.0), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.FRESH
        );

        // Even though soil is critically dry, pipe burst is active
        AgronomicAssessmentEngine.AssessmentInput inputBurst = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(dryMoisture), TelemetryTrend.stable(1, now, now),
                true, false, BigDecimal.valueOf(500), now
        );
        AgronomicAssessment assessment = assessmentEngine.assess(inputBurst);

        assertThat(assessment.isHydraulicSafetyBlocked()).isTrue();
        assertThat(assessment.getOverallSeverity()).isEqualTo(IntelligenceSeverity.CRITICAL);
        assertThat(assessment.getPrimarySignal()).isEqualTo(AgronomicSignalType.ABNORMAL_FLOW);

        // Check recommendation output
        IntelligenceRecommendation rec = recommendationEngine.generate(assessment, BigDecimal.valueOf(2.5));
        assertThat(rec.getRecommendationType()).isEqualTo("IRRIGATION_BLOCKED");
        assertThat(rec.getPriority()).isEqualTo(IntelligenceSeverity.CRITICAL);
        assertThat(rec.getRecommendedVolumeM3()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("AgronomicAssessmentEngine: water budget exhaustion enforces quota restriction")
    void waterBudgetExhaustionEnforcesQuotaRestriction() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();

        TelemetrySignal dryMoisture = new TelemetrySignal(
                fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(14.0), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.FRESH
        );

        AgronomicAssessmentEngine.AssessmentInput inputBudget = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(dryMoisture), TelemetryTrend.stable(1, now, now),
                false, false, BigDecimal.ZERO, now
        );
        AgronomicAssessment assessment = assessmentEngine.assess(inputBudget);

        assertThat(assessment.isWaterBudgetConstrained()).isTrue();

        IntelligenceRecommendation rec = recommendationEngine.generate(assessment, BigDecimal.valueOf(2.5));
        assertThat(rec.getRecommendationType()).isEqualTo("IRRIGATION_RESTRICTED");
        assertThat(rec.getPriority()).isEqualTo(IntelligenceSeverity.HIGH);
        assertThat(rec.getRecommendedVolumeM3()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("AgronomicAssessmentEngine: missing or invalid telemetry degrades confidence")
    void missingOrInvalidTelemetryDegradesConfidence() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();

        // 1. Invalid telemetry reading
        TelemetrySignal invalidMoisture = new TelemetrySignal(
                fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(12.0), MeasurementUnit.PERCENT,
                ReadingQuality.INVALID, now, TelemetryFreshness.FRESH
        );
        AgronomicAssessmentEngine.AssessmentInput inputInvalid = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(invalidMoisture), TelemetryTrend.stable(1, now, now),
                false, false, BigDecimal.valueOf(500), now
        );
        AgronomicAssessment assessmentInvalid = assessmentEngine.assess(inputInvalid);

        assertThat(assessmentInvalid.getPrimarySignal()).isEqualTo(AgronomicSignalType.NO_RECENT_TELEMETRY);
        assertThat(assessmentInvalid.getConfidence()).isEqualTo(IntelligenceConfidence.LOW);

        // 2. Missing telemetry reading
        AgronomicAssessmentEngine.AssessmentInput inputMissing = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(), TelemetryTrend.stable(0, null, null),
                false, false, BigDecimal.valueOf(500), now
        );
        AgronomicAssessment assessmentMissing = assessmentEngine.assess(inputMissing);

        assertThat(assessmentMissing.getPrimarySignal()).isEqualTo(AgronomicSignalType.NO_RECENT_TELEMETRY);
        assertThat(assessmentMissing.getConfidence()).isEqualTo(IntelligenceConfidence.LOW);

        IntelligenceRecommendation rec = recommendationEngine.generate(assessmentMissing, BigDecimal.valueOf(2.5));
        assertThat(rec.getRecommendationType()).isEqualTo("INSPECT_SENSOR");
    }

    @Test
    @DisplayName("IntelligenceRecommendationEngine: generates valid irrigation plan suggestion for dry soil")
    void recommendationGenerationForDrySoil() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();

        TelemetrySignal dryMoisture = new TelemetrySignal(
                fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(14.0), MeasurementUnit.PERCENT,
                ReadingQuality.VALID, now, TelemetryFreshness.FRESH
        );

        AgronomicAssessmentEngine.AssessmentInput input = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId, List.of(dryMoisture), TelemetryTrend.stable(3, now.minus(3, ChronoUnit.HOURS), now),
                false, false, BigDecimal.valueOf(1000), now
        );
        AgronomicAssessment assessment = assessmentEngine.assess(input);

        IntelligenceRecommendation rec = recommendationEngine.generate(assessment, BigDecimal.valueOf(3.0)); // 3 hectares
        assertThat(rec.getRecommendationType()).isEqualTo("IRRIGATION_RECOMMENDED");
        assertThat(rec.getRecommendedVolumeM3()).isEqualByComparingTo(new BigDecimal("1050.0000")); // 3 * 350
        assertThat(rec.getRecommendedDurationMinutes()).isEqualTo(240);
        assertThat(rec.getRecommendedWindowStart()).isAfter(now);
    }
}
