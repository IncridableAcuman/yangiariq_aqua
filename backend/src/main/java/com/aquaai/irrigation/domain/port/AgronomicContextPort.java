package com.aquaai.irrigation.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain port for weather and reference evapotranspiration (ET0) observations.
 */
public interface AgronomicContextPort {

    record MeteorologicalData(
            BigDecimal temperatureCelsius,
            BigDecimal relativeHumidityPercent,
            BigDecimal precipitationProbability,
            BigDecimal forecastRainMm,
            BigDecimal windSpeedMps,
            BigDecimal referenceEt0MmDay
    ) {}

    MeteorologicalData getMeteorologicalData(UUID fieldId);
}
