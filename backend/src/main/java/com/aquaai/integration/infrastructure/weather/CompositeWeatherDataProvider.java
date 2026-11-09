package com.aquaai.integration.infrastructure.weather;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.recommendation.domain.port.WeatherDataProvider;
import com.aquaai.recommendation.infrastructure.weather.SimulatedYangiariqWeatherProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Composite weather data provider seamlessly routing between verified external meteorological observation
 * APIs and simulated climate baselines while maintaining strict evidence traceability.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class CompositeWeatherDataProvider implements WeatherDataProvider {

    private final ExternalWeatherProperties properties;
    private final ExternalWeatherClient weatherClient;
    private final SimulatedYangiariqWeatherProvider simulatedWeatherProvider;
    private final DataSourceRepository dataSourceRepository;
    private final FieldRepository fieldRepository;

    @Override
    public WeatherSnapshot getWeatherForField(UUID fieldId) {
        if (properties.isEnabled()) {
            double lat = properties.getDistrictLatitude();
            double lon = properties.getDistrictLongitude();

            if (fieldId != null) {
                Optional<Field> fieldOpt = fieldRepository.findById(fieldId);
                if (fieldOpt.isPresent() && fieldOpt.get().getLocation() != null) {
                    if (fieldOpt.get().getLocation().latitude() != null) {
                        lat = fieldOpt.get().getLocation().latitude().doubleValue();
                    }
                    if (fieldOpt.get().getLocation().longitude() != null) {
                        lon = fieldOpt.get().getLocation().longitude().doubleValue();
                    }
                }
            }

            Optional<WeatherSnapshot> external = weatherClient.fetchCurrentWeather(lat, lon);
            if (external.isPresent()) {
                recordSyncSuccess();
                return external.get();
            }

            recordSyncFailure("External weather API unavailable or response invalid, falling back to simulated");
            log.warn("External weather provider unavailable for field {}. Safely falling back to SIMULATED weather.", fieldId);
        }

        // Return simulated snapshot with source clearly labeled as SIMULATED
        return simulatedWeatherProvider.getWeatherForField(fieldId);
    }

    private void recordSyncSuccess() {
        try {
            dataSourceRepository.findBySourceCode("OPEN_METEO_WEATHER").ifPresent(ds -> {
                ds.recordSyncSuccess(1);
                dataSourceRepository.save(ds);
            });
        } catch (Exception e) {
            log.debug("Could not record weather data source success: {}", e.getMessage());
        }
    }

    private void recordSyncFailure(String reason) {
        try {
            dataSourceRepository.findBySourceCode("OPEN_METEO_WEATHER").ifPresent(ds -> {
                ds.recordSyncFailure(reason, 1);
                dataSourceRepository.save(ds);
            });
        } catch (Exception e) {
            log.debug("Could not record weather data source failure: {}", e.getMessage());
        }
    }
}
