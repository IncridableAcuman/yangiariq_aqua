package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.service.PredictionOutcomeMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Prediction Method Version Traceability & Immutability Unit Tests")
class PredictionMethodVersionTraceabilityTest {

    @Test
    @DisplayName("Generated Prediction should contain method version tag and configuration snapshot")
    void shouldContainVersionTagAndConfigSnapshot() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant target = now.plusSeconds(86400);

        Prediction prediction = Prediction.create(
                fieldId,
                PredictionType.WATER_DEMAND_FORECAST,
                PredictionHorizon.NEXT_48_HOURS,
                target,
                BigDecimal.valueOf(150.0),
                BigDecimal.valueOf(130.0),
                BigDecimal.valueOf(170.0),
                "m3",
                88,
                RiskLevel.LOW,
                DataSufficiency.SUFFICIENT,
                IrrigationLikelihood.LOW_LIKELIHOOD,
                "Normal demand",
                "Assumed no rainfall",
                "Maintain schedule",
                "water",
                "WATER_DEMAND_RULE_V2",
                "{\"applicationEfficiency\":0.85}"
        );

        assertThat(prediction.getMethodVersionTag()).isEqualTo("WATER_DEMAND_RULE_V2");
        assertThat(prediction.getConfigurationSnapshot()).isEqualTo("{\"applicationEfficiency\":0.85}");
    }

    @Test
    @DisplayName("Prediction factory default fallback resolves standard baseline V1 tags")
    void shouldResolveDefaultBaselineV1Tags() {
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant target = now.plusSeconds(86400);

        Prediction soilMoisturePred = Prediction.create(
                fieldId,
                PredictionType.SOIL_MOISTURE_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                target,
                BigDecimal.valueOf(28.5),
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(32.0),
                "%",
                85,
                RiskLevel.LOW,
                DataSufficiency.SUFFICIENT,
                IrrigationLikelihood.LOW_LIKELIHOOD,
                "Stable trend",
                "",
                "",
                ""
        );

        assertThat(soilMoisturePred.getMethodVersionTag()).isEqualTo("SOIL_MOISTURE_TREND_V1");
        assertThat(soilMoisturePred.getConfigurationSnapshot()).isNotBlank();

        Prediction waterDemandPred = Prediction.create(
                fieldId,
                PredictionType.WATER_DEMAND_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                target,
                BigDecimal.valueOf(120.0),
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(140.0),
                "m3",
                85,
                RiskLevel.LOW,
                DataSufficiency.SUFFICIENT,
                IrrigationLikelihood.LOW_LIKELIHOOD,
                "Quota check",
                "",
                "",
                ""
        );

        assertThat(waterDemandPred.getMethodVersionTag()).isEqualTo("WATER_DEMAND_RULE_V1");

        Prediction irrigationNeedPred = Prediction.create(
                fieldId,
                PredictionType.IRRIGATION_NEED_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                target,
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(1.0),
                "boolean",
                90,
                RiskLevel.MEDIUM,
                DataSufficiency.SUFFICIENT,
                IrrigationLikelihood.HIGHLY_LIKELY,
                "Depletion above MAD",
                "",
                "",
                ""
        );

        assertThat(irrigationNeedPred.getMethodVersionTag()).isEqualTo("IRRIGATION_NEED_RULE_V1");
    }

    @Test
    @DisplayName("PredictionOutcomeMatcher must preserve historical prediction version tag during evaluation")
    void outcomeMatcherShouldPreserveHistoricalVersion() {
        PredictionOutcomeMatcher matcher = new PredictionOutcomeMatcher();

        Prediction legacyPrediction = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(UUID.randomUUID())
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .generatedAt(Instant.now().minusSeconds(100000))
                .targetTime(Instant.now().minusSeconds(10000))
                .predictedValue(BigDecimal.valueOf(25.0))
                .methodVersionTag("SOIL_MOISTURE_TREND_V1")
                .configurationSnapshot("{\"smoothingAlpha\":0.30}")
                .confidencePercent(80)
                .build();

        String resolvedVersion = matcher.resolveModelVersion(legacyPrediction);
        assertThat(resolvedVersion).isEqualTo("SOIL_MOISTURE_TREND_V1");

        Prediction v2Prediction = Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(UUID.randomUUID())
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .generatedAt(Instant.now().minusSeconds(50000))
                .targetTime(Instant.now().minusSeconds(5000))
                .predictedValue(BigDecimal.valueOf(26.0))
                .methodVersionTag("SOIL_MOISTURE_TREND_V2")
                .configurationSnapshot("{\"smoothingAlpha\":0.25}")
                .confidencePercent(85)
                .build();

        String resolvedV2 = matcher.resolveModelVersion(v2Prediction);
        assertThat(resolvedV2).isEqualTo("SOIL_MOISTURE_TREND_V2");
    }

    @Test
    @DisplayName("PredictionMethodVersion safety check blocks all hard hydraulic interlock keywords")
    void shouldBlockHardHydraulicSafetyKeywords() {
        String[] illegalConfigs = {
                "{\"pipeBurstPressureBar\": 6.5}",
                "{\"emergencyPressureThreshold\": 7.0}",
                "{\"hydraulicSafetyInterlock\": false}",
                "{\"emergencyStopOverride\": true}",
                "{\"safetyBlockEnabled\": false}",
                "{\"actuatorSafetyBypass\": true}",
                "{\"actuatorCommandForce\": \"OPEN\"}",
                "{\"valveOverride\": true}",
                "{\"pumpOverride\": true}"
        };

        for (String config : illegalConfigs) {
            assertThatThrownBy(() -> PredictionMethodVersion.validateSafetyParameters(config))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Hard hydraulic safety parameters cannot be modified");
        }
    }
}
