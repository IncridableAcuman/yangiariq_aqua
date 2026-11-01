package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case retrieving paginated historical telemetry observations across all sensors on a field.
 */
@Service
@RequiredArgsConstructor
public class ListFieldReadingsUseCase {

    private final SensorReadingRepository sensorReadingRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    public record Query(
            UUID fieldId,
            Instant from,
            Instant to,
            int page,
            int size,
            String sortDirection
    ) {
    }

    @Transactional(readOnly = true)
    public SensorReadingPage execute(Query query) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field read access
        fieldOwnershipChecker.getFieldForRead(query.fieldId(), currentUserId, currentRole);

        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new BusinessRuleException(
                    "Start date must be before or equal to end date",
                    "INVALID_DATE_RANGE"
            );
        }

        return sensorReadingRepository.findByFieldId(
                query.fieldId(),
                query.from(),
                query.to(),
                query.page(),
                query.size(),
                query.sortDirection()
        );
    }
}
