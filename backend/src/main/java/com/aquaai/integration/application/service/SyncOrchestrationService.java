package com.aquaai.integration.application.service;

import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.integration.infrastructure.weather.ExternalWeatherClient;
import com.aquaai.integration.infrastructure.weather.ExternalWeatherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncOrchestrationService {

    private final DataSourceRepository dataSourceRepository;
    private final ExternalWeatherClient weatherClient;
    private final ExternalWeatherProperties weatherProperties;

    private final AtomicBoolean isSyncRunning = new AtomicBoolean(false);

    /**
     * Periodic scheduled synchronization across operational external data sources.
     * Guarded by non-overlapping AtomicBoolean.
     */
    @Scheduled(fixedDelayString = "${app.integration.sync.interval-ms:300000}", initialDelay = 60000)
    public void runScheduledSync() {
        if (!isSyncRunning.compareAndSet(false, true)) {
            log.info("Previous synchronization execution still in progress. Skipping overlapping schedule.");
            return;
        }

        try {
            log.debug("Initiating scheduled external data source synchronization...");
            List<DataSource> sources = dataSourceRepository.findAll();
            for (DataSource source : sources) {
                if (source.getStatus() == DataSourceStatus.ACTIVE || source.getStatus() == DataSourceStatus.DEGRADED) {
                    syncSourceSafely(source);
                }
            }
        } catch (Exception e) {
            log.error("Unhandled error during data source synchronization cycle: {}", e.getMessage(), e);
        } finally {
            isSyncRunning.set(false);
        }
    }

    /**
     * Triggers immediate synchronization for a specific data source.
     */
    public boolean triggerManualSync(UUID dataSourceId) {
        DataSource source = dataSourceRepository.findById(dataSourceId)
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + dataSourceId));

        return syncSourceSafely(source);
    }

    private boolean syncSourceSafely(DataSource source) {
        log.info("Synchronizing data source '{}' (code: {}, type: {})...", source.getName(), source.getSourceCode(), source.getSourceType());

        try {
            if (source.getSourceType() == DataSourceType.WEATHER_PROVIDER) {
                return syncWeatherProvider(source);
            } else if (source.getSourceType() == DataSourceType.SIMULATED_PROVIDER) {
                // Heartbeat simulation provider
                source.recordSyncSuccess(0);
                dataSourceRepository.save(source);
                return true;
            } else if (source.getSourceType() == DataSourceType.SENSOR_DEVICE) {
                // For edge gateways without active hardware, keep NOT_CONFIGURED or record heartbeat
                if (source.getEndpointUrl() == null && (source.getConfigurationRef() == null || source.getConfigurationRef().contains("TOKEN"))) {
                    if (source.getStatus() != DataSourceStatus.ACTIVE) {
                        source.markNotConfigured();
                        dataSourceRepository.save(source);
                        return false;
                    }
                }
                source.recordSyncSuccess(0);
                dataSourceRepository.save(source);
                return true;
            } else {
                source.recordSyncSuccess(0);
                dataSourceRepository.save(source);
                return true;
            }
        } catch (Exception e) {
            log.warn("Synchronization failed for data source {}: {}", source.getSourceCode(), e.getMessage());
            source.recordSyncFailure(e.getMessage(), 0);
            dataSourceRepository.save(source);
            return false;
        }
    }

    private boolean syncWeatherProvider(DataSource source) {
        if ("OPEN_METEO_WEATHER".equals(source.getSourceCode())) {
            if (!weatherProperties.isEnabled()) {
                source.markNotConfigured();
                dataSourceRepository.save(source);
                return false;
            }

            var snapshotOpt = weatherClient.fetchCurrentWeather(
                    weatherProperties.getDistrictLatitude(),
                    weatherProperties.getDistrictLongitude()
            );

            if (snapshotOpt.isPresent()) {
                source.recordSyncSuccess(1);
                dataSourceRepository.save(source);
                log.info("Open-Meteo weather synchronization succeeded.");
                return true;
            } else {
                source.recordSyncFailure("External weather endpoint returned empty or invalid response", 1);
                dataSourceRepository.save(source);
                return false;
            }
        } else {
            source.recordSyncSuccess(0);
            dataSourceRepository.save(source);
            return true;
        }
    }
}
