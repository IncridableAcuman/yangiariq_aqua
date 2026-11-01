package com.aquaai.recommendation.domain.port;

import com.aquaai.recommendation.domain.model.WeatherSnapshot;

import java.util.UUID;

/**
 * Domain port for retrieving meteorological observations and short-term forecast snapshots.
 */
public interface WeatherDataProvider {

    WeatherSnapshot getWeatherForField(UUID fieldId);
}
