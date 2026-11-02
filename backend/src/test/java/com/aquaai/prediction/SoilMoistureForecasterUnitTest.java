package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator.SufficiencyEvaluation;
import com.aquaai.prediction.domain.service.SoilMoistureForecaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoilMoistureForecasterUnitTest {

    private SoilMoistureForecaster forecaster;
    private DataSufficiencyEvaluator sufficiencyEvaluator;
    private FieldMetadata field;
    private WeatherContext weather;
    private Instant now;

    @BeforeEach
    void setUp() {
        forecaster = new SoilMoistureForecaster();
        sufficiencyEvaluator = new DataSufficiencyEvaluator();
        now = Instant.now();
        field = new FieldMetadata(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Field 12 - Cotton",
                "COTTON",
                "LOAM",
                "DRIP",
                BigDecimal.valueOf(10.0),
                true
        );
        weather = new WeatherContext(
                BigDecimal.valueOf(32.0),
                BigDecimal.valueOf(30.0),
                BigDecimal.valueOf(5.5),
                BigDecimal.ZERO
        );
    }

    @Test
    @DisplayName("Insufficient telemetry produces UNKNOWN forecast without fabricated moisture")
    void insufficientTelemetryProducesUnavailable() {
        SufficiencyEvaluation sufficiency = sufficiencyEvaluator.evaluate(Collections.emptyList(), now);
        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, Collections.emptyList(), weather, sufficiency, now);

        assertThat(result.getDataSufficiency()).isEqualTo(DataSufficiency.INSUFFICIENT);
        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.UNKNOWN);
        assertThat(result.getPredictedValue()).isNull();
        assertThat(result.getConfidencePercent()).isZero();
    }

    @Test
    @DisplayName("Declining soil moisture produces accurate projected decline and realistic bounds")
    void decliningMoistureTrend() {
        List<TelemetryObservation> obs = new ArrayList<>();
        // Depleting from 32% to 28% over 24 hours (~0.167% / hr)
        for (int i = 0; i < 7; i++) {
            obs.add(new TelemetryObservation(
                    UUID.randomUUID(),
                    "SOIL_MOISTURE",
                    BigDecimal.valueOf(32.0 - (i * 0.6)),
                    "VALID",
                    now.minus(Duration.ofHours(24 - (i * 4)))
            ));
        }

        SufficiencyEvaluation sufficiency = sufficiencyEvaluator.evaluate(obs, now);
        Prediction result24h = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, obs, weather, sufficiency, now);
        Prediction result48h = forecaster.forecast(field, PredictionHorizon.NEXT_48_HOURS, obs, weather, sufficiency, now);

        assertThat(result24h.getPredictedValue()).isNotNull();
        assertThat(result24h.getPredictedValue()).isLessThan(BigDecimal.valueOf(28.5));
        assertThat(result48h.getPredictedValue()).isLessThan(result24h.getPredictedValue());

        // Confidence should degrade with longer horizon
        assertThat(result48h.getConfidencePercent()).isLessThan(result24h.getConfidencePercent());

        // Bounds must encapsulate predicted value
        assertThat(result24h.getLowerBound()).isLessThanOrEqualTo(result24h.getPredictedValue());
        assertThat(result24h.getUpperBound()).isGreaterThanOrEqualTo(result24h.getPredictedValue());
    }

    @Test
    @DisplayName("Moisture projected below critical threshold results in HIGH or CRITICAL risk")
    void criticalMoistureProjectionTriggersHighRisk() {
        List<TelemetryObservation> obs = new ArrayList<>();
        // Starting low: 22% dropping to 19%
        for (int i = 0; i < 6; i++) {
            obs.add(new TelemetryObservation(
                    UUID.randomUUID(),
                    "SOIL_MOISTURE",
                    BigDecimal.valueOf(22.0 - (i * 0.6)),
                    "VALID",
                    now.minus(Duration.ofHours(12 - (i * 2)))
            ));
        }

        SufficiencyEvaluation sufficiency = sufficiencyEvaluator.evaluate(obs, now);
        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, obs, weather, sufficiency, now);

        assertThat(result.getRiskLevel()).isIn(RiskLevel.HIGH, RiskLevel.CRITICAL);
        assertThat(result.getRecommendedAction()).contains("Prepare irrigation event");
    }
}
