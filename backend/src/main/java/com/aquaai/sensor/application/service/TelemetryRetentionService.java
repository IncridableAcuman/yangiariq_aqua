package com.aquaai.sensor.application.service;

import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service orchestrating historical telemetry retention policies and data lifecycle management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryRetentionService {

    private final SensorReadingRepository sensorReadingRepository;

    @Transactional
    public int purgeExpiredTelemetry(int retentionDays) {
        int safeDays = Math.max(retentionDays, 30); // safeguard: minimum 30 days
        Instant cutoff = Instant.now().minus(safeDays, ChronoUnit.DAYS);
        log.info("Executing telemetry retention purge for observations older than {} ({} days retention)", cutoff, safeDays);

        int deletedCount = sensorReadingRepository.deleteByMeasuredAtBefore(cutoff);
        log.info("Completed telemetry retention purge. Total records deleted: {}", deletedCount);
        return deletedCount;
    }
}
