package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * High-efficiency read model retrieving the latest telemetry reading for each sensor type deployed on a field.
 */
@Service
@RequiredArgsConstructor
public class GetFieldTelemetrySummaryUseCase {

    private final SensorReadingRepository sensorReadingRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<SensorReading> execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field read access
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        return sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId);
    }
}
