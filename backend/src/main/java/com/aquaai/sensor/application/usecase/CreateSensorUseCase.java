package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case orchestrating the creation and assignment of a new sensor device to an agricultural field.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSensorUseCase {

    private final SensorRepository sensorRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    public record Command(
            UUID fieldId,
            String name,
            String deviceIdentifier,
            SensorType type,
            String provider,
            String description
    ) {
    }

    @Transactional
    public Sensor execute(Command command) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field ownership / administrative authorization
        fieldOwnershipChecker.getFieldForMutation(command.fieldId(), currentUserId, currentRole);

        // Enforce global uniqueness of hardware device identifier
        if (sensorRepository.existsByDeviceIdentifier(command.deviceIdentifier())) {
            throw new DuplicateResourceException("Sensor", "deviceIdentifier", command.deviceIdentifier());
        }

        Sensor sensor = Sensor.create(
                command.fieldId(),
                command.name(),
                command.deviceIdentifier(),
                command.type(),
                command.provider(),
                command.description()
        );

        Sensor saved = sensorRepository.save(sensor);
        log.info("Sensor created: id={}, deviceIdentifier={}, type={}, fieldId={}, createdBy={}",
                saved.getId(), saved.getDeviceIdentifier(), saved.getType(), saved.getFieldId(), currentUserId);
        return saved;
    }
}
