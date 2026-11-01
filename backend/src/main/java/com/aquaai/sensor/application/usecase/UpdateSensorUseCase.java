package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case updating mutable sensor metadata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSensorUseCase {

    private final SensorRepository sensorRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    public record Command(
            String name,
            String description
    ) {
    }

    @Transactional
    public Sensor execute(UUID sensorId, Command command) {
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

        sensor.updateDetails(command.name(), command.description());
        Sensor updated = sensorRepository.save(sensor);
        log.info("Sensor updated: id={}, fieldId={}, updatedBy={}", updated.getId(), updated.getFieldId(), currentUserId);
        return updated;
    }
}
