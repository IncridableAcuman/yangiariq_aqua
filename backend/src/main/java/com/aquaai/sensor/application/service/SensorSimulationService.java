package com.aquaai.sensor.application.service;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.application.event.TelemetryReadingRecordedEvent;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.provider.SensorProvider;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controlled application service orchestrating telemetry simulation runs.
 * Provides on-demand telemetry generation for demo, integration testing, and local operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorSimulationService {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorProvider sensorProvider;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Triggers instantaneous telemetry observation generation for a specific sensor.
     */
    @Transactional
    public SensorReading simulateReadingForSensor(UUID sensorId) {
        if (sensorId == null) {
            throw new ResourceNotFoundException("Sensor", "null");
        }

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        try {
            fieldOwnershipChecker.getFieldForMutation(sensor.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Sensor", sensorId);
        }

        if (!sensor.isActive()) {
            throw new BusinessRuleException(
                    "Cannot simulate readings for sensor with status " + sensor.getStatus(),
                    "SENSOR_NOT_ACTIVE"
            );
        }

        SensorReading reading = sensorProvider.read(sensor);
        SensorReading saved = sensorReadingRepository.save(reading);

        sensor.recordHeartbeat(saved.getMeasuredAt());
        sensorRepository.save(sensor);

        eventPublisher.publishEvent(new TelemetryReadingRecordedEvent(saved));

        log.info("Simulated telemetry reading recorded: sensorId={}, type={}, value={} {}",
                sensor.getId(), sensor.getType(), saved.getValue(), saved.getUnit());
        return saved;
    }

    /**
     * Triggers telemetry observation generation for all active sensors deployed on a field.
     */
    @Transactional
    public List<SensorReading> simulateReadingsForField(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        fieldOwnershipChecker.getFieldForMutation(fieldId, currentUserId, currentRole);

        List<Sensor> activeSensors = sensorRepository.findByFieldId(fieldId).stream()
                .filter(Sensor::isActive)
                .toList();

        List<SensorReading> generated = new ArrayList<>();
        for (Sensor sensor : activeSensors) {
            SensorReading reading = sensorProvider.read(sensor);
            SensorReading saved = sensorReadingRepository.save(reading);
            sensor.recordHeartbeat(saved.getMeasuredAt());
            sensorRepository.save(sensor);
            eventPublisher.publishEvent(new TelemetryReadingRecordedEvent(saved));
            generated.add(saved);
        }

        log.info("Simulated {} telemetry readings for fieldId={}", generated.size(), fieldId);
        return generated;
    }

    /**
     * Triggers simulation across all active simulated sensors in the district (Administrative task).
     */
    @Transactional
    public List<SensorReading> simulateAllActiveSensors() {
        List<Sensor> activeSimulated = sensorRepository.findAllActiveSimulated();
        List<SensorReading> generated = new ArrayList<>();

        for (Sensor sensor : activeSimulated) {
            SensorReading reading = sensorProvider.read(sensor);
            SensorReading saved = sensorReadingRepository.save(reading);
            sensor.recordHeartbeat(saved.getMeasuredAt());
            sensorRepository.save(sensor);
            eventPublisher.publishEvent(new TelemetryReadingRecordedEvent(saved));
            generated.add(saved);
        }

        log.info("Batch simulated {} telemetry readings across all active sensors", generated.size());
        return generated;
    }
}
