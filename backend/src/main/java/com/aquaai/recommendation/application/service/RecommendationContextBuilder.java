package com.aquaai.recommendation.application.service;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.recommendation.domain.port.WeatherDataProvider;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.model.IrrigationPlanStatus;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Analytical context builder that queries cross-bounded contexts (Field, Sensor,
 * Anomaly, Water, Weather) and consolidates them into an immutable
 * {@link AgronomicRecommendationContext} for downstream engines and AI adapters.
 */
@Service
@RequiredArgsConstructor
public class RecommendationContextBuilder {

    private final FieldRepository fieldRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final AnomalyRepository anomalyRepository;
    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;
    private final IrrigationPlanRepository irrigationPlanRepository;
    private final WeatherDataProvider weatherDataProvider;

    @Transactional(readOnly = true)
    public AgronomicRecommendationContext buildContext(UUID fieldId) {
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        // 1. Gather latest telemetry readings by sensor type
        List<SensorReading> latestReadings = sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId);
        Map<SensorType, SensorReading> readingsByType = new HashMap<>();
        for (SensorReading reading : latestReadings) {
            readingsByType.put(reading.getType(), reading);
        }

        SensorReading moistureReading = readingsByType.get(SensorType.SOIL_MOISTURE);
        SensorReading flowReading = readingsByType.get(SensorType.WATER_FLOW);
        SensorReading pressureReading = readingsByType.get(SensorType.PRESSURE);
        SensorReading salinityReading = readingsByType.get(SensorType.SALINITY);
        SensorReading tempReading = readingsByType.get(SensorType.TEMPERATURE);
        SensorReading humidityReading = readingsByType.get(SensorType.HUMIDITY);

        BigDecimal soilMoisture = moistureReading != null ? moistureReading.getValue() : null;
        ReadingQuality moistureQuality = moistureReading != null ? moistureReading.getQuality() : null;
        BigDecimal waterFlow = flowReading != null ? flowReading.getValue() : null;
        BigDecimal waterPressure = pressureReading != null ? pressureReading.getValue() : null;
        BigDecimal salinity = salinityReading != null ? salinityReading.getValue() : null;
        BigDecimal ambientTemp = tempReading != null ? tempReading.getValue() : null;
        BigDecimal ambientHumidity = humidityReading != null ? humidityReading.getValue() : null;

        // 2. Gather active anomalies for the field
        List<Anomaly> activeAnomalies = new ArrayList<>();
        List<Anomaly> detectedAnomalies = anomalyRepository.findAll(
                fieldId, null, AnomalyStatus.DETECTED, null, null, null, null, null, 0, 50
        );
        List<Anomaly> acknowledgedAnomalies = anomalyRepository.findAll(
                fieldId, null, AnomalyStatus.ACKNOWLEDGED, null, null, null, null, null, 0, 50
        );
        activeAnomalies.addAll(detectedAnomalies);
        activeAnomalies.addAll(acknowledgedAnomalies);

        // 3. Water delivery & scheduling status
        Instant now = Instant.now();
        Instant pastWeek = now.minus(7, ChronoUnit.DAYS);
        FieldWaterSummaryData waterSummary = waterConsumptionQueryRepository.getFieldWaterSummary(fieldId, pastWeek, now);
        BigDecimal recentDeliveredM3 = waterSummary != null && waterSummary.totalConsumedVolumeM3() != null
                ? waterSummary.totalConsumedVolumeM3()
                : BigDecimal.ZERO;

        List<IrrigationPlan> inProgressPlans = irrigationPlanRepository.findByFieldIdAndStatus(fieldId, IrrigationPlanStatus.IN_PROGRESS);
        boolean isScheduledActive = !inProgressPlans.isEmpty();

        // 4. Weather snapshot
        WeatherSnapshot weather = weatherDataProvider.getWeatherForField(fieldId);

        // 5. Evaluate simulation marker
        boolean isSimulated = activeAnomalies.stream().anyMatch(Anomaly::isSimulated)
                || (moistureReading != null && moistureReading.getSource() != null && moistureReading.getSource().contains("SIMULAT"));

        return AgronomicRecommendationContext.builder()
                .fieldId(field.getId())
                .fieldName(field.getName())
                .cropType(field.getCropType())
                .soilType(field.getSoilType())
                .fieldAreaHectares(field.getAreaHectares())
                .irrigationMethod(field.getIrrigationMethod())
                .soilMoisturePercent(soilMoisture)
                .soilMoistureQuality(moistureQuality)
                .soilMoistureTrend1h(BigDecimal.ZERO)
                .waterFlowM3H(waterFlow)
                .waterPressureBar(waterPressure)
                .salinityDsM(salinity)
                .ambientTemperatureC(ambientTemp)
                .ambientHumidityPercent(ambientHumidity)
                .activeAnomalies(activeAnomalies)
                .isScheduledIrrigationActive(isScheduledActive)
                .recentWaterDeliveredM3(recentDeliveredM3)
                .weather(weather)
                .evaluationTime(now)
                .isSimulated(isSimulated)
                .build();
    }
}
