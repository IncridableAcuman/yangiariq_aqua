package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.AgriculturalSeason;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Domain Service constructing standardized, versioned feature vectors (FEAT_SOIL_MOISTURE_V1).
 * Features are strictly computed from past operational data available at forecast time T (anti-leakage).
 */
public class FeatureExtractor {

    public static final int FEATURE_COUNT = 14;

    public double[] extractSoilMoistureFeatures(
            FieldMetadata field,
            List<TelemetryObservation> observations,
            WeatherContext weather,
            WaterBalance waterBalance,
            BigDecimal recentIrrigationVolume7d,
            Instant forecastTime
    ) {
        double[] x = new double[FEATURE_COUNT];

        // 1. Current Moisture & 2. 24h Trend Slope
        List<TelemetryObservation> validObs = observations != null
                ? observations.stream()
                .filter(o -> !"INVALID".equalsIgnoreCase(o.quality()))
                .sorted(Comparator.comparing(TelemetryObservation::measuredAt))
                .toList()
                : List.of();

        double currentMoisture = 25.0; // agronomic default
        double slope24h = -0.15; // default depletion rate %/hr
        double validRatio = 1.0;

        if (!validObs.isEmpty()) {
            currentMoisture = validObs.get(validObs.size() - 1).value().doubleValue();
            if (validObs.size() >= 2) {
                slope24h = calculateSlopePerHour(validObs);
            }
            if (observations != null && !observations.isEmpty()) {
                long validCount = observations.stream().filter(o -> !"INVALID".equalsIgnoreCase(o.quality())).count();
                validRatio = (double) validCount / observations.size();
            }
        }

        x[0] = currentMoisture;
        x[1] = slope24h;

        // 3. Temperature & 4. Relative Humidity & 5. Reference ET0 & 6. Forecast Rain
        x[2] = (weather != null && weather.temperatureC() != null) ? weather.temperatureC().doubleValue() : 28.0;
        x[3] = (weather != null && weather.relativeHumidityPercent() != null) ? weather.relativeHumidityPercent().doubleValue() : 45.0;
        x[4] = (weather != null && weather.referenceEt0MmDay() != null) ? weather.referenceEt0MmDay().doubleValue() : 5.0;
        x[5] = (weather != null && weather.forecastRainMm() != null) ? weather.forecastRainMm().doubleValue() : 0.0;

        // 7. Crop Type Ordinal
        x[6] = encodeCropType(field != null ? field.cropType() : null);

        // 8. Soil Type Ordinal
        x[7] = encodeSoilType(field != null ? field.soilType() : null);

        // 9. Irrigation Method Ordinal
        x[8] = encodeIrrigationMethod(field != null ? field.irrigationMethod() : null);

        // 10. Field Area Hectares
        x[9] = (field != null && field.areaHectares() != null) ? field.areaHectares().doubleValue() : 5.0;

        // 11. Recent Irrigation Volume (past 7 days)
        x[10] = recentIrrigationVolume7d != null ? recentIrrigationVolume7d.doubleValue() : 0.0;

        // 12. Water Headroom %
        double headroomPercent = 100.0;
        if (waterBalance != null && waterBalance.allocatedVolumeM3() != null && waterBalance.allocatedVolumeM3().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal consumed = waterBalance.consumedVolumeM3() != null ? waterBalance.consumedVolumeM3() : BigDecimal.ZERO;
            BigDecimal remaining = waterBalance.allocatedVolumeM3().subtract(consumed).max(BigDecimal.ZERO);
            headroomPercent = remaining.doubleValue() / waterBalance.allocatedVolumeM3().doubleValue() * 100.0;
        }
        x[11] = headroomPercent;

        // 13. Season Code
        x[12] = encodeSeason(forecastTime != null ? forecastTime : Instant.now());

        // 14. Telemetry Quality Score
        x[13] = validRatio;

        return x;
    }

    private double calculateSlopePerHour(List<TelemetryObservation> obs) {
        double firstSec = obs.get(0).measuredAt().getEpochSecond();
        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
        int n = obs.size();
        for (TelemetryObservation o : obs) {
            double xHours = (o.measuredAt().getEpochSecond() - firstSec) / 3600.0;
            double yVal = o.value().doubleValue();
            sumX += xHours;
            sumY += yVal;
            sumXX += xHours * xHours;
            sumXY += xHours * yVal;
        }
        double denominator = (n * sumXX - sumX * sumX);
        if (Math.abs(denominator) > 1e-6) {
            double slope = (n * sumXY - sumX * sumY) / denominator;
            if (slope < -0.45) slope = -0.45;
            if (slope > 0.80) slope = 0.80;
            return slope;
        }
        return -0.15;
    }

    private double encodeCropType(String crop) {
        if (crop == null) return 0.0;
        String c = crop.toUpperCase();
        if (c.contains("COTTON")) return 1.0;
        if (c.contains("WHEAT")) return 2.0;
        if (c.contains("MAIZE") || c.contains("CORN")) return 3.0;
        if (c.contains("ORCHARD") || c.contains("FRUIT") || c.contains("GARDEN")) return 4.0;
        return 0.0;
    }

    private double encodeSoilType(String soil) {
        if (soil == null) return 0.0;
        String s = soil.toUpperCase();
        if (s.contains("SANDY")) return 1.0;
        if (s.contains("CLAY")) return 2.0;
        if (s.contains("LOAM")) return 3.0;
        if (s.contains("SILT")) return 4.0;
        return 0.0;
    }

    private double encodeIrrigationMethod(String method) {
        if (method == null) return 0.0;
        String m = method.toUpperCase();
        if (m.contains("DRIP")) return 1.0;
        if (m.contains("SPRINKLER")) return 2.0;
        if (m.contains("FURROW")) return 3.0;
        if (m.contains("FLOOD")) return 4.0;
        return 0.0;
    }

    private double encodeSeason(Instant time) {
        ZonedDateTime zdt = time.atZone(ZoneId.of("Asia/Tashkent"));
        int month = zdt.getMonthValue();
        return switch (month) {
            case 3, 4, 5 -> 1.0; // Spring
            case 6, 7, 8 -> 2.0; // Summer
            case 9, 10, 11 -> 3.0; // Autumn
            default -> 4.0; // Winter
        };
    }
}
