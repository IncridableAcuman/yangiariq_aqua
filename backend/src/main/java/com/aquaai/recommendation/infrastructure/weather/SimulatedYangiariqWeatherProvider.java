package com.aquaai.recommendation.infrastructure.weather;

import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.recommendation.domain.port.WeatherDataProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Regional simulated weather data provider tailored for Yangiariq district, Khorezm (Uzbekistan).
 * Simulates characteristic continental arid climate conditions:
 * - High summer temperatures (36 - 42°C) during chilla with garmsel dry winds
 * - Low relative humidity (15 - 35%)
 * - Negligible summer precipitation with occasional dry convection
 * - Winter cold snaps (-10 to 5°C)
 */
@Component
public class SimulatedYangiariqWeatherProvider implements WeatherDataProvider {

    @Override
    public WeatherSnapshot getWeatherForField(UUID fieldId) {
        Instant now = Instant.now();
        ZonedDateTime zdt = now.atZone(ZoneOffset.UTC);
        int month = zdt.getMonthValue();
        int hour = zdt.getHour();

        // Baseline seasonal temperatures for Yangiariq
        double baseTemp;
        double baseHumidity;
        double rainProb;
        double rainAmountMm;

        if (month >= 6 && month <= 8) {
            // Summer (Chilla): Hot, dry, arid
            baseTemp = 34.0 + (hour >= 11 && hour <= 16 ? 6.5 : 0.0);
            baseHumidity = 20.0 + (hour < 7 ? 10.0 : 0.0);
            rainProb = 0.05;
            rainAmountMm = 0.0;
        } else if (month >= 3 && month <= 5) {
            // Spring: Mild, moderate winds
            baseTemp = 20.0 + (hour >= 12 && hour <= 15 ? 4.0 : -2.0);
            baseHumidity = 45.0;
            rainProb = 0.15;
            rainAmountMm = 1.2;
        } else if (month >= 9 && month <= 10) {
            // Autumn: Harvest period, cool nights, pleasant days
            baseTemp = 18.0 + (hour >= 12 && hour <= 15 ? 5.0 : -4.0);
            baseHumidity = 40.0;
            rainProb = 0.10;
            rainAmountMm = 0.5;
        } else {
            // Winter: Cold, dry continental
            baseTemp = 1.0 + (hour >= 12 && hour <= 14 ? 3.0 : -5.0);
            baseHumidity = 70.0;
            rainProb = 0.20;
            rainAmountMm = 2.0;
        }

        // Apply a small deterministic perturbation per field to avoid identical readings across parcels
        if (fieldId != null) {
            int hashOffset = Math.abs(fieldId.hashCode() % 100);
            double deltaTemp = (hashOffset / 50.0) - 1.0; // [-1.0, +1.0]
            baseTemp += deltaTemp;
        }

        return WeatherSnapshot.builder()
                .temperature(BigDecimal.valueOf(baseTemp).setScale(1, RoundingMode.HALF_UP))
                .relativeHumidity(BigDecimal.valueOf(baseHumidity).setScale(1, RoundingMode.HALF_UP))
                .precipitationProbability(BigDecimal.valueOf(rainProb).setScale(2, RoundingMode.HALF_UP))
                .precipitationAmountMm(BigDecimal.valueOf(rainAmountMm).setScale(1, RoundingMode.HALF_UP))
                .windSpeedMps(BigDecimal.valueOf(4.2).setScale(1, RoundingMode.HALF_UP))
                .atmosphericPressureHpa(BigDecimal.valueOf(1012.5).setScale(1, RoundingMode.HALF_UP))
                .timestamp(now)
                .source("SIMULATED_YANGIARIQ_WEATHER")
                .build();
    }
}
