package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.AdaptiveSelectionResult;
import com.aquaai.prediction.domain.model.AgriculturalSeason;
import com.aquaai.prediction.domain.model.AnomalySeverityContext;
import com.aquaai.prediction.domain.model.ContextMatchLevel;
import com.aquaai.prediction.domain.model.ContextPerformanceProfile;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.PredictionContext;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.SensorAvailabilityStatus;
import com.aquaai.prediction.domain.model.TelemetryQualityLevel;
import com.aquaai.prediction.domain.model.WaterAvailabilityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdaptiveSelectionEngineTest {

    private AdaptiveSelectionEngine engine;
    private UUID fieldId;
    private PredictionMethodVersion activeBaseline;
    private PredictionMethodVersion candidateVersion;

    @BeforeEach
    void setUp() {
        engine = new AdaptiveSelectionEngine();
        fieldId = UUID.randomUUID();

        activeBaseline = new PredictionMethodVersion(
                UUID.randomUUID(),
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_V1",
                MethodVersionStatus.ACTIVE,
                "Active Baseline",
                "{\"madThresholdFraction\":0.45}",
                false,
                Instant.now(),
                Instant.now(),
                null,
                "admin",
                "admin",
                0L
        );

        candidateVersion = new PredictionMethodVersion(
                UUID.randomUUID(),
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_V2",
                MethodVersionStatus.APPROVED,
                "Candidate Version",
                "{\"madThresholdFraction\":0.40}",
                false,
                Instant.now(),
                null,
                null,
                "admin",
                null,
                0L
        );
    }

    @Test
    @DisplayName("Level 1 Safety Dominance: Critical hydraulic anomaly forces safety interlock dominant fallback")
    void level1SafetyInterlockDominance() {
        PredictionContext context = PredictionContext.builder()
                .fieldId(fieldId)
                .cropType("COTTON")
                .soilType("SANDY_LOAM")
                .irrigationMethod("DRIP")
                .telemetryQuality(TelemetryQualityLevel.HIGH)
                .sensorAvailability(SensorAvailabilityStatus.COMPLETE)
                .waterAvailability(WaterAvailabilityContext.NORMAL)
                .recentAnomalySeverity(AnomalySeverityContext.CRITICAL) // CRITICAL ANOMALY
                .season(AgriculturalSeason.SUMMER)
                .build();

        AdaptiveSelectionResult result = engine.selectBestMethod(
                PredictionType.SOIL_MOISTURE_FORECAST,
                context,
                List.of(activeBaseline, candidateVersion),
                Map.of()
        );

        assertThat(result.contextMatchLevel()).isEqualTo(ContextMatchLevel.BASELINE_FALLBACK);
        assertThat(result.selectedMethod()).contains("SAFETY_INTERLOCK_DOMINANT");
        assertThat(result.selectedVersionTag()).isEqualTo(activeBaseline.getVersionTag());
        assertThat(result.selectionReason()).contains("Hydraulic safety interlock active");
    }

    @Test
    @DisplayName("Level 4 Sensor Availability: Missing sensors force INSUFFICIENT_DATA fallback with degraded confidence")
    void level4MissingSensorsFallback() {
        PredictionContext context = PredictionContext.builder()
                .fieldId(fieldId)
                .cropType("COTTON")
                .soilType("SANDY_LOAM")
                .irrigationMethod("DRIP")
                .telemetryQuality(TelemetryQualityLevel.INSUFFICIENT)
                .sensorAvailability(SensorAvailabilityStatus.MISSING) // MISSING SENSORS
                .waterAvailability(WaterAvailabilityContext.NORMAL)
                .recentAnomalySeverity(AnomalySeverityContext.NONE)
                .season(AgriculturalSeason.SUMMER)
                .build();

        AdaptiveSelectionResult result = engine.selectBestMethod(
                PredictionType.SOIL_MOISTURE_FORECAST,
                context,
                List.of(activeBaseline, candidateVersion),
                Map.of()
        );

        assertThat(result.contextMatchLevel()).isEqualTo(ContextMatchLevel.INSUFFICIENT_DATA);
        assertThat(result.selectedMethod()).contains("INSUFFICIENT_DATA");
        assertThat(result.confidencePercent()).isEqualTo(20);
    }

    @Test
    @DisplayName("Level 5 & 6 Context Match & Performance: Candidate with lower MAE on exact context is selected")
    void selectsBetterPerformingCandidateOnExactContext() {
        PredictionContext context = PredictionContext.builder()
                .fieldId(fieldId)
                .cropType("COTTON")
                .soilType("SANDY_LOAM")
                .irrigationMethod("DRIP")
                .telemetryQuality(TelemetryQualityLevel.HIGH)
                .sensorAvailability(SensorAvailabilityStatus.COMPLETE)
                .waterAvailability(WaterAvailabilityContext.NORMAL)
                .recentAnomalySeverity(AnomalySeverityContext.NONE)
                .season(AgriculturalSeason.SUMMER)
                .build();

        ContextPerformanceProfile baselineProfile = ContextPerformanceProfile.create(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_V1",
                "COTTON", "SANDY_LOAM", "DRIP",
                TelemetryQualityLevel.HIGH, AgriculturalSeason.SUMMER,
                20,
                BigDecimal.valueOf(14.0), // Baseline MAE = 14.0
                BigDecimal.valueOf(16.0),
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(78.0),
                ModelHealthStatus.HEALTHY
        );

        ContextPerformanceProfile candidateProfile = ContextPerformanceProfile.create(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_V2",
                "COTTON", "SANDY_LOAM", "DRIP",
                TelemetryQualityLevel.HIGH, AgriculturalSeason.SUMMER,
                20,
                BigDecimal.valueOf(8.5), // Candidate MAE = 8.5 (better)
                BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(0.5),
                BigDecimal.valueOf(91.0),
                ModelHealthStatus.HEALTHY
        );

        AdaptiveSelectionResult result = engine.selectBestMethod(
                PredictionType.SOIL_MOISTURE_FORECAST,
                context,
                List.of(activeBaseline, candidateVersion),
                Map.of(
                        "SOIL_MOISTURE_V1", List.of(baselineProfile),
                        "SOIL_MOISTURE_V2", List.of(candidateProfile)
                )
        );

        assertThat(result.selectedVersionTag()).isEqualTo("SOIL_MOISTURE_V2");
        assertThat(result.contextMatchLevel()).isEqualTo(ContextMatchLevel.EXACT_CONTEXT);
        assertThat(result.sampleSize()).isEqualTo(20);
        assertThat(result.selectionReason()).contains("Method SOIL_MOISTURE_V2 selected based on exact context");
    }

    @Test
    @DisplayName("Level 8 Contextual Drift: Degraded candidate is rejected, falling back to active baseline")
    void rejectsDegradedCandidateUnderDrift() {
        PredictionContext context = PredictionContext.builder()
                .fieldId(fieldId)
                .cropType("COTTON")
                .soilType("SANDY_LOAM")
                .irrigationMethod("DRIP")
                .telemetryQuality(TelemetryQualityLevel.HIGH)
                .sensorAvailability(SensorAvailabilityStatus.COMPLETE)
                .waterAvailability(WaterAvailabilityContext.NORMAL)
                .recentAnomalySeverity(AnomalySeverityContext.NONE)
                .season(AgriculturalSeason.SUMMER)
                .build();

        ContextPerformanceProfile baselineProfile = ContextPerformanceProfile.create(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_V1",
                "COTTON", "SANDY_LOAM", "DRIP",
                TelemetryQualityLevel.HIGH, AgriculturalSeason.SUMMER,
                20,
                BigDecimal.valueOf(11.0),
                BigDecimal.valueOf(13.0),
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(84.0),
                ModelHealthStatus.HEALTHY
        );

        // Candidate has high MAE and drift detected
        ContextPerformanceProfile degradedCandidateProfile = ContextPerformanceProfile.builder()
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .versionTag("SOIL_MOISTURE_V2")
                .cropType("COTTON")
                .soilType("SANDY_LOAM")
                .irrigationMethod("DRIP")
                .telemetryQuality(TelemetryQualityLevel.HIGH)
                .season(AgriculturalSeason.SUMMER)
                .sampleSize(20)
                .hasSufficientSample(true)
                .meanAbsoluteError(BigDecimal.valueOf(18.0))
                .rootMeanSquaredError(BigDecimal.valueOf(20.0))
                .meanBiasError(BigDecimal.valueOf(12.0))
                .accuracyPercent(BigDecimal.valueOf(65.0))
                .globalModelHealth(ModelHealthStatus.HEALTHY)
                .contextualModelHealth(ModelHealthStatus.DEGRADED)
                .contextDriftDetected(true)
                .driftDiagnosis("Contextual drift detected")
                .build();

        AdaptiveSelectionResult result = engine.selectBestMethod(
                PredictionType.SOIL_MOISTURE_FORECAST,
                context,
                List.of(activeBaseline, candidateVersion),
                Map.of(
                        "SOIL_MOISTURE_V1", List.of(baselineProfile),
                        "SOIL_MOISTURE_V2", List.of(degradedCandidateProfile)
                )
        );

        assertThat(result.selectedVersionTag()).isEqualTo("SOIL_MOISTURE_V1");
    }

    @Test
    @DisplayName("Level 9 Safe Fallback: When no contextual profiles exist, safely uses active baseline")
    void fallbackToActiveBaselineWhenNoProfiles() {
        PredictionContext context = PredictionContext.builder()
                .fieldId(fieldId)
                .cropType("VINEYARD")
                .soilType("LOAM")
                .irrigationMethod("SURFACE")
                .telemetryQuality(TelemetryQualityLevel.HIGH)
                .sensorAvailability(SensorAvailabilityStatus.COMPLETE)
                .waterAvailability(WaterAvailabilityContext.NORMAL)
                .recentAnomalySeverity(AnomalySeverityContext.NONE)
                .season(AgriculturalSeason.SUMMER)
                .build();

        AdaptiveSelectionResult result = engine.selectBestMethod(
                PredictionType.SOIL_MOISTURE_FORECAST,
                context,
                List.of(activeBaseline, candidateVersion),
                Map.of() // empty profiles
        );

        assertThat(result.selectedVersionTag()).isEqualTo("SOIL_MOISTURE_V1");
        assertThat(result.contextMatchLevel()).isEqualTo(ContextMatchLevel.BASELINE_FALLBACK);
    }
}
