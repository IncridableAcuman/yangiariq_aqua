package com.aquaai.recommendation.infrastructure.engine;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicAgronomicEngineTest {

    private final DeterministicAgronomicEngineImpl engine = new DeterministicAgronomicEngineImpl();

    @Test
    @DisplayName("Critical Soil Moisture Deficit: Generates IRRIGATE_NOW with CRITICAL priority")
    void shouldGenerateCriticalIrrigateNowForSevereDeficit() {
        UUID fieldId = UUID.randomUUID();
        AgronomicRecommendationContext context = AgronomicRecommendationContext.builder()
                .fieldId(fieldId)
                .fieldName("Paxtakor 1")
                .cropType(CropType.COTTON)
                .soilType(SoilType.LOAM)
                .fieldAreaHectares(BigDecimal.valueOf(10.0))
                .irrigationMethod(IrrigationMethod.DRIP)
                .soilMoisturePercent(BigDecimal.valueOf(11.5))
                .soilMoistureQuality(ReadingQuality.VALID)
                .soilMoistureTrend1h(BigDecimal.valueOf(-3.5))
                .isSimulated(false)
                .build();

        List<AgronomicRecommendation> results = engine.evaluate(context);

        assertThat(results).hasSize(1);
        AgronomicRecommendation rec = results.get(0);
        assertThat(rec.getRecommendationType()).isEqualTo(RecommendationType.IRRIGATE_NOW);
        assertThat(rec.getPriority()).isEqualTo(RecommendationPriority.CRITICAL);
        assertThat(rec.getActions()).isNotEmpty();
        assertThat(rec.getEvidence()).isNotEmpty();
    }

    @Test
    @DisplayName("Hydraulic Safety Interlock: Suspected pipe burst produces INSPECT_PIPE_BURST and suppresses irrigation")
    void shouldPrioritizePipeBurstInspectionOverIrrigation() {
        UUID fieldId = UUID.randomUUID();

        AnomalyCandidate candidate = new AnomalyCandidate(
                AnomalyType.SUSPECTED_PIPE_BURST,
                AnomalySeverity.CRITICAL,
                BigDecimal.valueOf(55.0),
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(30.0),
                BigDecimal.valueOf(0.92),
                "Suspected pipe burst",
                "Flow spike + pressure drop",
                "HYDRAULIC_ENGINE",
                List.of(),
                false
        );
        Anomaly pipeBurstAnomaly = Anomaly.create(fieldId, UUID.randomUUID(), SensorType.WATER_FLOW, candidate, Instant.now());

        AgronomicRecommendationContext context = AgronomicRecommendationContext.builder()
                .fieldId(fieldId)
                .fieldName("Paxtakor 2")
                .cropType(CropType.WHEAT)
                .soilType(SoilType.CLAY)
                .fieldAreaHectares(BigDecimal.valueOf(8.0))
                .irrigationMethod(IrrigationMethod.CANAL)
                .soilMoisturePercent(BigDecimal.valueOf(12.0)) // low moisture!
                .soilMoistureQuality(ReadingQuality.VALID)
                .waterFlowM3H(BigDecimal.valueOf(55.0))
                .waterPressureBar(BigDecimal.valueOf(0.8))
                .activeAnomalies(List.of(pipeBurstAnomaly))
                .isSimulated(false)
                .build();

        List<AgronomicRecommendation> results = engine.evaluate(context);

        assertThat(results).hasSize(1);
        AgronomicRecommendation rec = results.get(0);
        assertThat(rec.getRecommendationType()).isEqualTo(RecommendationType.INSPECT_PIPE_BURST);
        assertThat(rec.getPriority()).isEqualTo(RecommendationPriority.CRITICAL);
        // Crucial invariant: Irrigation is suppressed during pipe burst!
        assertThat(results).noneMatch(r -> r.getRecommendationType() == RecommendationType.IRRIGATE_NOW);
    }

    @Test
    @DisplayName("Expected Rainfall: Delays irrigation when significant precipitation is forecasted and soil is safe")
    void shouldDelayIrrigationWhenHeavyRainForecasted() {
        UUID fieldId = UUID.randomUUID();

        WeatherSnapshot weather = WeatherSnapshot.builder()
                .temperature(BigDecimal.valueOf(26.0))
                .relativeHumidity(BigDecimal.valueOf(65.0))
                .precipitationProbability(BigDecimal.valueOf(85.0))
                .precipitationAmountMm(BigDecimal.valueOf(22.0))
                .timestamp(Instant.now())
                .source("YANGIARIQ_METEO")
                .build();

        AgronomicRecommendationContext context = AgronomicRecommendationContext.builder()
                .fieldId(fieldId)
                .fieldName("Bog' 3")
                .cropType(CropType.MELON)
                .soilType(SoilType.SANDY)
                .fieldAreaHectares(BigDecimal.valueOf(5.0))
                .soilMoisturePercent(BigDecimal.valueOf(17.5)) // not critical!
                .soilMoistureQuality(ReadingQuality.VALID)
                .weather(weather)
                .isSimulated(false)
                .build();

        List<AgronomicRecommendation> results = engine.evaluate(context);

        assertThat(results).anyMatch(r -> r.getRecommendationType() == RecommendationType.DELAY_IRRIGATION
                && r.getPriority() == RecommendationPriority.HIGH);
    }

    @Test
    @DisplayName("Moisture Saturation: Generates AVOID_IRRIGATION to prevent root hypoxia")
    void shouldAvoidIrrigationWhenSoilIsSaturated() {
        UUID fieldId = UUID.randomUUID();

        AgronomicRecommendationContext context = AgronomicRecommendationContext.builder()
                .fieldId(fieldId)
                .fieldName("Sholi 4")
                .cropType(CropType.RICE)
                .soilType(SoilType.CLAY)
                .fieldAreaHectares(BigDecimal.valueOf(15.0))
                .soilMoisturePercent(BigDecimal.valueOf(38.5)) // saturated
                .soilMoistureQuality(ReadingQuality.VALID)
                .isSimulated(false)
                .build();

        List<AgronomicRecommendation> results = engine.evaluate(context);

        assertThat(results).anyMatch(r -> r.getRecommendationType() == RecommendationType.AVOID_IRRIGATION
                && r.getPriority() == RecommendationPriority.MEDIUM);
    }

    @Test
    @DisplayName("Salinity Hazard: Generates MANAGE_SALINITY when electrical conductivity is high")
    void shouldGenerateSalinityManagementWhenSalinityExceedsThreshold() {
        UUID fieldId = UUID.randomUUID();

        AgronomicRecommendationContext context = AgronomicRecommendationContext.builder()
                .fieldId(fieldId)
                .fieldName("Saline Parcel 5")
                .cropType(CropType.COTTON)
                .soilType(SoilType.LOAM)
                .fieldAreaHectares(BigDecimal.valueOf(12.0))
                .soilMoisturePercent(BigDecimal.valueOf(22.0))
                .soilMoistureQuality(ReadingQuality.VALID)
                .salinityDsM(BigDecimal.valueOf(5.4)) // high salinity
                .isSimulated(false)
                .build();

        List<AgronomicRecommendation> results = engine.evaluate(context);

        assertThat(results).anyMatch(r -> r.getRecommendationType() == RecommendationType.MANAGE_SALINITY
                && r.getPriority() == RecommendationPriority.HIGH);
    }
}
