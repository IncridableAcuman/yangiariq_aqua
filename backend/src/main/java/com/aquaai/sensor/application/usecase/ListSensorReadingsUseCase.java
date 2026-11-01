package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case retrieving paginated historical telemetry observations for a specific sensor.
 */
@Service
@RequiredArgsConstructor
public class ListSensorReadingsUseCase {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    public record Query(
            UUID sensorId,
            Instant from,
            Instant to,
            int page,
            int size,
            String sortDirection
    ) {
    }

    @Transactional(readOnly = true)
    public SensorReadingPage execute(Query query) {
        if (query.sensorId() == null) {
            throw new ResourceNotFoundException("Sensor", "null");
        }

        Sensor sensor = sensorRepository.findById(query.sensorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", query.sensorId()));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        try {
            fieldOwnershipChecker.getFieldForRead(sensor.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Sensor", query.sensorId());
        }

        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new BusinessRuleException(
                    "Start date must be before or equal to end date",
                    "INVALID_DATE_RANGE"
            );
        }

        return sensorReadingRepository.findBySensorId(
                query.sensorId(),
                query.from(),
                query.to(),
                query.page(),
                query.size(),
                query.sortDirection()
        );
    }
}
