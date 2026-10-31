package com.aquaai.field.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Deactivates an agricultural field parcel.
 * Performs a domain lifecycle transition (ACTIVE -> INACTIVE) rather than physical hard deletion.
 * Preserves historical records for irrigation, sensor telemetry, and incident reporting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeactivateFieldUseCase {

    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker ownershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        Field field = ownershipChecker.getFieldForMutation(fieldId, currentUserId, currentRole);

        field.deactivate();
        fieldRepository.save(field);
        log.info("Field {} deactivated successfully by user {}", fieldId, currentUserId);
    }
}
