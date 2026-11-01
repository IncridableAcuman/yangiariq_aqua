package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.application.event.TelemetryReadingRecordedEvent;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Use case recording a validated telemetry observation for a sensor device.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordSensorReadingUseCase {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    public record Command(
            UUID sensorId,
            BigDecimal value,
            MeasurementUnit unit,
            ReadingQuality quality,
            String source,
            Instant measuredAt
    ) {
    }

    @Transactional
    public SensorReading execute(Command command) {
        if (command.sensorId() == null) {
            throw new ResourceNotFoundException("Sensor", "null");
        }

        Sensor sensor = sensorRepository.findById(command.sensorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", command.sensorId()));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field-level mutation access for manual/client recording
        try {
            fieldOwnershipChecker.getFieldForMutation(sensor.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Sensor", command.sensorId());
        }

        if (!sensor.isActive()) {
            throw new BusinessRuleException(
                    "Cannot record telemetry observation for sensor with status " + sensor.getStatus(),
                    "SENSOR_NOT_ACTIVE"
            );
        }

        MeasurementUnit unit = command.unit() != null ? command.unit() : sensor.getType().getDefaultUnit();
        SensorReading reading = SensorReading.create(
                sensor.getId(),
                sensor.getFieldId(),
                sensor.getType(),
                command.value(),
                unit,
                command.quality(),
                command.source(),
                command.measuredAt()
        );

        SensorReading saved = sensorReadingRepository.save(reading);

        // Update sensor last seen heartbeat
        sensor.recordHeartbeat(saved.getMeasuredAt());
        sensorRepository.save(sensor);

        // Publish event for asynchronous/decoupled anomaly evaluation
        eventPublisher.publishEvent(new TelemetryReadingRecordedEvent(saved));

        log.debug("Telemetry reading recorded: sensorId={}, type={}, value={} {}, quality={}",
                sensor.getId(), sensor.getType(), saved.getValue(), saved.getUnit(), saved.getQuality());
        return saved;
    }
}
