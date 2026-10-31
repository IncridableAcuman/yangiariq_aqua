package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Reactivates an inactive field parcel.
 * Validates name uniqueness among active fields before restoring ACTIVE status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactivateFieldUseCase {

    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker ownershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FieldResponse execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        Field field = ownershipChecker.getFieldForMutation(fieldId, currentUserId, currentRole);

        if (fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                field.getOwnerId(), field.getName(), FieldStatus.ACTIVE, field.getId())) {
            throw new DuplicateResourceException("An active field with name '" + field.getName() + "' already exists for this farmer");
        }

        field.activate();
        Field saved = fieldRepository.save(field);
        log.info("Field {} reactivated successfully by user {}", fieldId, currentUserId);
        return FieldResponse.from(saved);
    }
}
