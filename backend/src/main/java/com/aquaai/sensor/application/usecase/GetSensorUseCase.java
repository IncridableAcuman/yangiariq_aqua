package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for retrieving a sensor aggregate by ID with strict anti-IDOR authorization.
 */
@Service
@RequiredArgsConstructor
public class GetSensorUseCase {

    private final SensorRepository sensorRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Sensor execute(UUID sensorId) {
        if (sensorId == null) {
            throw new ResourceNotFoundException("Sensor", "null");
        }

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        try {
            // Anti-IDOR enforcement: verify read access to underlying field
            fieldOwnershipChecker.getFieldForRead(sensor.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            // Anti-enumeration: do not reveal that the sensor exists on another farmer's parcel
            throw new ResourceNotFoundException("Sensor", sensorId);
        }

        return sensor;
    }
}
