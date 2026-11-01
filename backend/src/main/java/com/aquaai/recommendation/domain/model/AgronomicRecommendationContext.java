package com.aquaai.recommendation.domain.model;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.sensor.domain.model.ReadingQuality;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Analytical context bundle encapsulating all parcel, sensor, water, anomaly,
 * and weather data required to generate an agronomic recommendation.
 */
@Getter
@Builder
public class AgronomicRecommendationContext {

    private final UUID fieldId;
    private final String fieldName;
    private final CropType cropType;
    private final SoilType soilType;
    private final BigDecimal fieldAreaHectares;
    private final IrrigationMethod irrigationMethod;

    private final BigDecimal soilMoisturePercent;
    private final ReadingQuality soilMoistureQuality;
    private final BigDecimal soilMoistureTrend1h;
    private final BigDecimal waterFlowM3H;
    private final BigDecimal waterPressureBar;
    private final BigDecimal salinityDsM;
    private final BigDecimal ambientTemperatureC;
    private final BigDecimal ambientHumidityPercent;

    @Builder.Default
    private final List<Anomaly> activeAnomalies = new ArrayList<>();

    private final boolean isScheduledIrrigationActive;
    private final BigDecimal recentWaterDeliveredM3;
    private final WeatherSnapshot weather;
    private final Instant evaluationTime;
    private final boolean isSimulated;

    public List<Anomaly> getActiveAnomalies() {
        return Collections.unmodifiableList(activeAnomalies);
    }

    public boolean hasAnomaly(String anomalyTypeName) {
        if (activeAnomalies == null || activeAnomalies.isEmpty()) {
            return false;
        }
        return activeAnomalies.stream()
                .anyMatch(a -> a.getAnomalyType().name().equalsIgnoreCase(anomalyTypeName));
    }
}
