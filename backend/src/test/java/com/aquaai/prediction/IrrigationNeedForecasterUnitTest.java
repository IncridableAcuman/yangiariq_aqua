package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.service.IrrigationNeedForecaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IrrigationNeedForecasterUnitTest {

    private IrrigationNeedForecaster forecaster;
    private FieldMetadata field;
    private WeatherContext weather;
    private Instant now;

    @BeforeEach
    void setUp() {
        forecaster = new IrrigationNeedForecaster();
        now = Instant.now();
        field = new FieldMetadata(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Field 4 - Wheat",
                "WHEAT",
                "SANDY_LOAM",
                "SPRINKLER",
                BigDecimal.valueOf(5.0),
                true
        );
        weather = new WeatherContext(
                BigDecimal.valueOf(30.0),
                BigDecimal.valueOf(35.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.ZERO
        );
    }

    @Test
    @DisplayName("Moisture projected below MAD threshold triggers HIGHLY_LIKELY likelihood with volumetric calculation")
    void highlyLikelyIrrigationNeed() {
        Prediction soilMoisturePred = Prediction.create(
                field.fieldId(),
                PredictionType.SOIL_MOISTURE_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                now.plusSeconds(86400),
                BigDecimal.valueOf(18.0), // Below MAD (22% for Wheat)
                BigDecimal.valueOf(16.0),
                BigDecimal.valueOf(20.0),
                "%",
                80,
                RiskLevel.HIGH,
                DataSufficiency.SUFFICIENT,
                null,
                "Moisture depleting",
                "No rain",
                "Irrigate",
                "irrigation"
        );

        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, soilMoisturePred, weather, now);

        assertThat(result.getLikelihood()).isEqualTo(IrrigationLikelihood.HIGHLY_LIKELY);
        assertThat(result.getPredictedValue()).isNotNull();
        assertThat(result.getPredictedValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getUnit()).isEqualTo("m³");
        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    @DisplayName("Moisture well above MAD threshold triggers NOT_EXPECTED likelihood")
    void notExpectedIrrigationNeed() {
        Prediction soilMoisturePred = Prediction.create(
                field.fieldId(),
                PredictionType.SOIL_MOISTURE_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                now.plusSeconds(86400),
                BigDecimal.valueOf(34.0), // Well above MAD
                BigDecimal.valueOf(32.0),
                BigDecimal.valueOf(36.0),
                "%",
                85,
                RiskLevel.LOW,
                DataSufficiency.SUFFICIENT,
                null,
                "Moisture optimal",
                "No rain",
                "Optimal",
                "irrigation"
        );

        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, soilMoisturePred, weather, now);

        assertThat(result.getLikelihood()).isEqualTo(IrrigationLikelihood.NOT_EXPECTED);
        assertThat(result.getPredictedValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    @DisplayName("Insufficient telemetry produces UNKNOWN irrigation need without fake numbers")
    void unavailableOnInsufficientTelemetry() {
        Prediction unavailableMoisture = Prediction.createUnavailable(field.fieldId(), PredictionType.SOIL_MOISTURE_FORECAST, PredictionHorizon.NEXT_24_HOURS, "Telemetry stale");
        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, unavailableMoisture, weather, now);

        assertThat(result.getLikelihood()).isEqualTo(IrrigationLikelihood.UNKNOWN);
        assertThat(result.getPredictedValue()).isNull();
        assertThat(result.getConfidencePercent()).isZero();
    }
}
