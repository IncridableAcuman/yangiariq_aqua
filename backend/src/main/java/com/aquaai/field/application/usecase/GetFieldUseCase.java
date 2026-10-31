package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Retrieves field parcel details by ID.
 * Protects against IDOR by enforcing query-level ownership verification:
 * Farmers can only access fields they own; Operators and Admins have district access.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetFieldUseCase {

    private final FieldOwnershipChecker ownershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public FieldResponse execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        Field field = ownershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);
        return FieldResponse.from(field);
    }
}
