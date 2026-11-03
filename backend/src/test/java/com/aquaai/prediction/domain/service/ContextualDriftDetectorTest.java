package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.AgriculturalSeason;
import com.aquaai.prediction.domain.model.ContextPerformanceProfile;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.TelemetryQualityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ContextualDriftDetectorTest {

    private ContextualDriftDetector driftDetector;

    @BeforeEach
    void setUp() {
        driftDetector = new ContextualDriftDetector();
    }

    @Test
    @DisplayName("Sample gating: skips drift evaluation when sample size n < 10")
    void sampleGatingUnder10() {
        ContextPerformanceProfile profile = ContextPerformanceProfile.create(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_V1",
                "COTTON",
                "SANDY_LOAM",
                "DRIP",
                TelemetryQualityLevel.HIGH,
                AgriculturalSeason.SUMMER,
                8, // n < 10
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(28.0),
                BigDecimal.valueOf(15.0),
                BigDecimal.valueOf(60.0),
                ModelHealthStatus.HEALTHY
        );

        ContextualDriftDetector.DriftEvaluation result = driftDetector.evaluate(
                profile, BigDecimal.valueOf(10.0), BigDecimal.ZERO
        );

        assertThat(result.isDriftDetected()).isFalse();
        assertThat(result.diagnosis()).contains("Insufficient contextual sample size");
    }

    @Test
    @DisplayName("Detects contextual drift when contextual MAE is >= 30% higher than global baseline")
    void detectsMaeDegradationDrift() {
        ContextPerformanceProfile profile = ContextPerformanceProfile.create(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_V1",
                "COTTON",
                "SANDY_LOAM",
                "DRIP",
                TelemetryQualityLevel.HIGH,
                AgriculturalSeason.SUMMER,
                25, // n >= 10
                BigDecimal.valueOf(14.5), // contextual MAE
                BigDecimal.valueOf(16.0),
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(82.0),
                ModelHealthStatus.HEALTHY
        );

        BigDecimal globalMae = BigDecimal.valueOf(10.0); // 14.5 / 10.0 = 1.45 (+45% increase >= 30%)
        BigDecimal globalMbe = BigDecimal.valueOf(1.0);

        ContextualDriftDetector.DriftEvaluation result = driftDetector.evaluate(profile, globalMae, globalMbe);

        assertThat(result.isDriftDetected()).isTrue();
        assertThat(result.errorDegradationPercent()).isEqualByComparingTo("45.0");
        assertThat(result.diagnosis()).contains("Contextual MAE is 45.0% higher than global baseline");
    }

    @Test
    @DisplayName("Detects contextual drift when Mean Bias Error deviates significantly (> 8%)")
    void detectsBiasShiftDrift() {
        ContextPerformanceProfile profile = ContextPerformanceProfile.create(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_V1",
                "COTTON",
                "CLAY",
                "FLOOD",
                TelemetryQualityLevel.HIGH,
                AgriculturalSeason.SUMMER,
                15,
                BigDecimal.valueOf(11.0),
                BigDecimal.valueOf(13.0),
                BigDecimal.valueOf(11.5), // high bias
                BigDecimal.valueOf(85.0),
                ModelHealthStatus.HEALTHY
        );

        BigDecimal globalMae = BigDecimal.valueOf(10.0);
        BigDecimal globalMbe = BigDecimal.valueOf(1.0); // |11.5 - 1.0| = 10.5 >= 8.0

        ContextualDriftDetector.DriftEvaluation result = driftDetector.evaluate(profile, globalMae, globalMbe);

        assertThat(result.isDriftDetected()).isTrue();
        assertThat(result.diagnosis()).contains("Contextual Mean Bias Error");
    }
}
