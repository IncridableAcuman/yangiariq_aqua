package com.aquaai.anomaly.application.service;

import com.aquaai.anomaly.application.port.AnomalyNotificationPort;
import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalyEvidence;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.anomaly.infrastructure.config.AnomalyProperties;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorPage;
import com.aquaai.sensor.domain.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Scheduled background service evaluating heartbeat timeouts and hardware offline states.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalySchedulerService {

    private final SensorRepository sensorRepository;
    private final AnomalyRepository anomalyRepository;
    private final AnomalyNotificationPort notificationPort;
    private final AnomalyProperties properties;

    @Scheduled(fixedDelayString = "${aquaai.anomaly.scheduler.interval:60000}", initialDelay = 15000)
    @Transactional
    public void evaluateOfflineSensors() {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            Instant now = Instant.now();
            Instant cutoff = now.minus(Duration.ofMinutes(properties.getOfflineThresholdMinutes()));

            // Inspect all active sensors
            SensorPage allSensors = sensorRepository.findAll(0, 500);
            for (Sensor sensor : allSensors.content()) {
                if (sensor.isActive() && (sensor.getLastSeenAt() == null || sensor.getLastSeenAt().isBefore(cutoff))) {
                    processOfflineSensor(sensor, now, cutoff);
                }
            }
        } catch (Exception e) {
            log.error("Error during scheduled offline sensor evaluation: {}", e.getMessage(), e);
        }
    }

    private void processOfflineSensor(Sensor sensor, Instant now, Instant cutoff) {
        Optional<Anomaly> activeOpt = anomalyRepository.findActiveByFieldAndSensorAndType(
                sensor.getFieldId(), sensor.getId(), AnomalyType.SENSOR_OFFLINE
        );

        long silenceMinutes = sensor.getLastSeenAt() != null
                ? Duration.between(sensor.getLastSeenAt(), now).toMinutes()
                : properties.getOfflineThresholdMinutes();

        String explanation = String.format("Datchik (%s) soʻnggi %d daqiqa davomida telemetriya maʼlumotlarini yubormadi (Heartbeat uzilgan). Batareya zaryadsizlanishi yoki aloqa signali yoʻqolishi ehtimoli bor.",
                sensor.getName(), silenceMinutes);

        if (activeOpt.isPresent()) {
            Anomaly existing = activeOpt.get();
            existing.recordRecurrence(
                    BigDecimal.valueOf(silenceMinutes),
                    explanation,
                    List.of(AnomalyEvidence.of("HEARTBEAT_TIMEOUT", "MINUTES_OFFLINE", BigDecimal.valueOf(silenceMinutes), null, now, null)),
                    now
            );
            anomalyRepository.save(existing);
        } else {
            AnomalyCandidate candidate = new AnomalyCandidate(
                    AnomalyType.SENSOR_OFFLINE,
                    AnomalySeverity.MEDIUM,
                    BigDecimal.valueOf(silenceMinutes),
                    BigDecimal.ZERO,
                    BigDecimal.valueOf(silenceMinutes),
                    BigDecimal.valueOf(0.90),
                    "Datchik aloqasi uzilgan (Oflayn)",
                    explanation,
                    "SENSOR_HEALTH",
                    List.of(AnomalyEvidence.of("HEARTBEAT_TIMEOUT", "MINUTES_OFFLINE", BigDecimal.valueOf(silenceMinutes), null, now, null)),
                    "SIMULATED".equalsIgnoreCase(sensor.getProvider())
            );

            Anomaly anomaly = Anomaly.create(sensor.getFieldId(), sensor.getId(), sensor.getType(), candidate, now);
            Anomaly saved = anomalyRepository.save(anomaly);
            notificationPort.sendAlert(new AnomalyNotificationPort.AnomalyNotification(
                    saved.getId(), saved.getFieldId(), saved.getSensorId(), saved.getAnomalyType(),
                    saved.getSeverity(), saved.getDescription(), saved.getExplanation(), saved.isSimulated(), now
            ));
        }
    }
}
