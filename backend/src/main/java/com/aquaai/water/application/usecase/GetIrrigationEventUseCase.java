package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationEventResponse;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Retrieves details for a specific irrigation event.
 * Enforces ownership: Farmers access only events on parcels they own (404 anti-enumeration).
 */
@Service
@RequiredArgsConstructor
public class GetIrrigationEventUseCase {

    private final IrrigationEventRepository irrigationEventRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public IrrigationEventResponse execute(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("IrrigationEvent", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        IrrigationEvent event = irrigationEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IrrigationEvent", id));

        try {
            fieldOwnershipChecker.getFieldForRead(event.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("IrrigationEvent", id);
        }

        return IrrigationEventResponse.from(event);
    }
}
