package com.aquaai.irrigation.infrastructure.adapter;

import com.aquaai.irrigation.domain.port.AgronomicContextPort;
import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.recommendation.domain.port.EvapotranspirationCalculator;
import com.aquaai.recommendation.domain.port.WeatherDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AgronomicContextAdapter implements AgronomicContextPort {

    private final WeatherDataProvider weatherDataProvider;
    private final EvapotranspirationCalculator evapotranspirationCalculator;

    @Override
    public MeteorologicalData getMeteorologicalData(UUID fieldId) {
        WeatherSnapshot weather = weatherDataProvider.getWeatherForField(fieldId);

        BigDecimal et0 = BigDecimal.valueOf(5.0);
        if (weather != null && evapotranspirationCalculator != null) {
            et0 = evapotranspirationCalculator.calculateReferenceET(weather, Instant.now());
        }

        if (weather == null) {
            return new MeteorologicalData(
                    BigDecimal.valueOf(28.0),
                    BigDecimal.valueOf(45.0),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.valueOf(3.5),
                    et0
            );
        }

        return new MeteorologicalData(
                weather.getTemperature(),
                weather.getRelativeHumidity(),
                weather.getPrecipitationProbability(),
                weather.getPrecipitationAmountMm(),
                weather.getWindSpeedMps(),
                et0
        );
    }
}
