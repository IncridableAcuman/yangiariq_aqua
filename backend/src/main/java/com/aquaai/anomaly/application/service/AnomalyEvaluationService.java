package com.aquaai.anomaly.application.service;

import com.aquaai.anomaly.application.port.AnomalyNotificationPort;
import com.aquaai.anomaly.domain.detection.AnomalyDetector;
import com.aquaai.anomaly.domain.detection.DetectionContext;
import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.anomaly.domain.repository.AnomalyRuleConfigRepository;
import com.aquaai.anomaly.infrastructure.config.AnomalyProperties;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorAnalyticsRepository;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core orchestration engine for anomaly evaluation, deduplication, cooldown,
 * and intelligent notification dispatching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyEvaluationService {

    private final AnomalyRepository anomalyRepository;
    private final AnomalyRuleConfigRepository ruleConfigRepository;
    private final List<AnomalyDetector> detectors;
    private final AnomalyNotificationPort notificationPort;
    private final AnomalyProperties properties;

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorAnalyticsRepository sensorAnalyticsRepository;
    private final IrrigationEventRepository irrigationEventRepository;
    private final FieldRepository fieldRepository;

    /**
     * Listens for application telemetry recording events and triggers evaluation.
     */
    @org.springframework.context.event.EventListener
    public void onTelemetryReading(com.aquaai.sensor.application.event.TelemetryReadingRecordedEvent event) {
        if (event != null && event.reading() != null) {
            evaluateReading(event.reading());
        }
    }

    /**
     * Evaluates a newly recorded telemetry observation against all applicable detectors.
     */
    @Transactional
    public List<Anomaly> evaluateReading(SensorReading reading) {
        if (!properties.isEnabled() || reading == null) {
            return List.of();
        }

        // False-positive prevention: Skip agricultural risk detection for INVALID raw readings
        // (Invalid readings are either rejected at gateway or handled separately)
        UUID fieldId = reading.getFieldId();
        UUID sensorId = reading.getSensorId();
        SensorType sensorType = reading.getType();
        Instant now = reading.getMeasuredAt() != null ? reading.getMeasuredAt() : Instant.now();

        // 1. Gather Context
        DetectionContext context = buildContext(reading, fieldId, sensorId, sensorType, now);

        // 2. Execute Detectors
        List<AnomalyCandidate> candidates = new ArrayList<>();
        for (AnomalyDetector detector : detectors) {
            if (detector.supports(sensorType)) {
                try {
                    List<AnomalyCandidate> detected = detector.detect(context);
                    if (detected != null && !detected.isEmpty()) {
                        candidates.addAll(detected);
                    }
                } catch (Exception e) {
                    log.error("Error executing detector {} for reading {}: {}", detector.getClass().getSimpleName(), reading.getId(), e.getMessage(), e);
                }
            }
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        // 3. Deduplication & Alert Management
        List<Anomaly> resultAnomalies = new ArrayList<>();
        for (AnomalyCandidate candidate : candidates) {
            Anomaly anomaly = processCandidate(fieldId, sensorId, sensorType, candidate, now);
            if (anomaly != null) {
                resultAnomalies.add(anomaly);
            }
        }

        return resultAnomalies;
    }

    /**
     * Processes an anomaly candidate with atomic deduplication and cooldown enforcement.
     */
    private Anomaly processCandidate(
            UUID fieldId,
            UUID sensorId,
            SensorType sensorType,
            AnomalyCandidate candidate,
            Instant now
    ) {
        if (!properties.isDeduplicationEnabled()) {
            Anomaly fresh = Anomaly.create(fieldId, sensorId, sensorType, candidate, now);
            Anomaly saved = anomalyRepository.save(fresh);
            dispatchNotification(saved);
            return saved;
        }

        // Check if an active anomaly already exists
        Optional<Anomaly> activeOpt = anomalyRepository.findActiveByFieldAndSensorAndType(
                fieldId, sensorId, candidate.getAnomalyType()
        );

        if (activeOpt.isPresent()) {
            Anomaly existing = activeOpt.get();
            existing.recordRecurrence(
                    candidate.getDetectedValue(),
                    candidate.getExplanation(),
                    candidate.getEvidenceList(),
                    now
            );

            // Check cooldown period for notification dispatch
            boolean shouldNotify = existing.getLastNotifiedAt() == null ||
                    Duration.between(existing.getLastNotifiedAt(), now).toMinutes() >= properties.getCooldownMinutes();

            if (shouldNotify) {
                dispatchNotification(existing);
                existing.markNotified(now);
            }

            return anomalyRepository.save(existing);
        }

        // Create new active anomaly
        try {
            Anomaly newAnomaly = Anomaly.create(fieldId, sensorId, sensorType, candidate, now);
            dispatchNotification(newAnomaly);
            newAnomaly.markNotified(now);
            return anomalyRepository.save(newAnomaly);
        } catch (DataIntegrityViolationException e) {
            // Concurrency-safety: another thread created the active anomaly concurrently
            log.warn("Concurrent active anomaly detected for fieldId={}, sensorId={}, type={}. Updating existing.",
                    fieldId, sensorId, candidate.getAnomalyType());
            return anomalyRepository.findActiveByFieldAndSensorAndType(fieldId, sensorId, candidate.getAnomalyType())
                    .map(existing -> {
                        existing.recordRecurrence(candidate.getDetectedValue(), candidate.getExplanation(), candidate.getEvidenceList(), now);
                        return anomalyRepository.save(existing);
                    })
                    .orElse(null);
        }
    }

    private void dispatchNotification(Anomaly anomaly) {
        try {
            notificationPort.sendAlert(new AnomalyNotificationPort.AnomalyNotification(
                    anomaly.getId(),
                    anomaly.getFieldId(),
                    anomaly.getSensorId(),
                    anomaly.getAnomalyType(),
                    anomaly.getSeverity(),
                    anomaly.getDescription(),
                    anomaly.getExplanation(),
                    anomaly.isSimulated(),
                    anomaly.getDetectedAt()
            ));
        } catch (Exception e) {
            log.error("Failed to dispatch anomaly notification for id={}: {}", anomaly.getId(), e.getMessage());
        }
    }

    private DetectionContext buildContext(
            SensorReading reading,
            UUID fieldId,
            UUID sensorId,
            SensorType sensorType,
            Instant now
    ) {
        // Field information
        String cropType = null;
        String soilType = null;
        if (fieldId != null) {
            Optional<Field> fieldOpt = fieldRepository.findById(fieldId);
            if (fieldOpt.isPresent()) {
                cropType = fieldOpt.get().getCropType() != null ? fieldOpt.get().getCropType().name() : null;
                soilType = fieldOpt.get().getSoilType() != null ? fieldOpt.get().getSoilType().name() : null;
            }
        }

        // Active irrigation check
        boolean hasActiveIrrigation = false;
        if (fieldId != null) {
            List<IrrigationEvent> events = irrigationEventRepository.findByFieldId(fieldId);
            hasActiveIrrigation = events.stream().anyMatch(e -> e.getStatus() == IrrigationEventStatus.STARTED);
        }

        // Line pressure reading on the same field (for multi-metric pipe burst detection)
        BigDecimal linePressure = null;
        if (fieldId != null) {
            List<Sensor> fieldSensors = sensorRepository.findByFieldId(fieldId);
            for (Sensor s : fieldSensors) {
                if (s.getType() == SensorType.PRESSURE && s.isActive()) {
                    Optional<SensorReading> latestP = sensorReadingRepository.findLatestBySensorId(s.getId());
                    if (latestP.isPresent() && latestP.get().getQuality() == ReadingQuality.VALID) {
                        linePressure = latestP.get().getValue();
                        break;
                    }
                }
            }
        }

        // Historical readings (for moving average, rate of change, stuck detection)
        List<DetectionContext.HistoricalReadingPoint> recentPoints = new ArrayList<>();
        if (sensorId != null) {
            List<SensorReading> recentReadings = sensorReadingRepository.findBySensorId(
                    sensorId, null, null, 0, 15, "DESC"
            ).content();
            for (SensorReading r : recentReadings) {
                recentPoints.add(new DetectionContext.HistoricalReadingPoint(r.getValue(), r.getQuality(), r.getMeasuredAt()));
            }
        }

        // Baseline statistics (24h aggregates)
        BigDecimal mean = null;
        BigDecimal stdDev = null;
        if (sensorId != null) {
            Instant from = now.minus(properties.getHistoricalWindowHours(), ChronoUnit.HOURS);
            List<TelemetryAggregate> aggregates = sensorAnalyticsRepository.aggregateBySensor(
                    sensorId, from, now, AggregationInterval.HOURLY, ReadingQuality.VALID
            );
            if (!aggregates.isEmpty()) {
                BigDecimal sum = BigDecimal.ZERO;
                int count = 0;
                for (TelemetryAggregate a : aggregates) {
                    if (a.average() != null) {
                        sum = sum.add(a.average());
                        count++;
                    }
                }
                if (count > 0) {
                    mean = sum.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
                    // Standard deviation estimation across aggregate points
                    if (count >= properties.getMinimumReadingsForStats()) {
                        double sumSq = 0;
                        for (TelemetryAggregate a : aggregates) {
                            if (a.average() != null) {
                                double diff = a.average().subtract(mean).doubleValue();
                                sumSq += (diff * diff);
                            }
                        }
                        stdDev = BigDecimal.valueOf(Math.sqrt(sumSq / count)).setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                }
            }
        }

        // Config rule override
        AnomalyRuleConfig ruleConfig = null;
        List<AnomalyRuleConfig> rules = ruleConfigRepository.findBySensorType(sensorType);
        if (!rules.isEmpty()) {
            ruleConfig = rules.get(0);
        }

        boolean isSimulated = "SIMULATED".equalsIgnoreCase(reading.getSource());

        return DetectionContext.builder()
                .fieldId(fieldId)
                .sensorId(sensorId)
                .sensorType(sensorType)
                .currentReadingValue(reading.getValue())
                .currentReadingQuality(reading.getQuality())
                .currentReadingTime(now)
                .isSimulated(isSimulated)
                .recentReadings(recentPoints)
                .baselineMean(mean)
                .baselineStdDev(stdDev)
                .ruleConfig(ruleConfig)
                .hasActiveIrrigationEvent(hasActiveIrrigation)
                .linePressureBar(linePressure)
                .cropType(cropType)
                .soilType(soilType)
                .build();
    }
}
