package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CancelOperationRequest;
import com.aquaai.water.api.dto.IrrigationEventResponse;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Cancels an in-progress irrigation delivery operation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelIrrigationEventUseCase {

    private final IrrigationEventRepository irrigationEventRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IrrigationEventResponse execute(UUID id, CancelOperationRequest request) {
        if (id == null) {
            throw new ResourceNotFoundException("IrrigationEvent", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        IrrigationEvent event = irrigationEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IrrigationEvent", id));

        try {
            fieldOwnershipChecker.getFieldForMutation(event.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("IrrigationEvent", id);
        }

        event.cancel(request != null ? request.reason() : null);
        IrrigationEvent saved = irrigationEventRepository.save(event);

        log.info("Irrigation event {} cancelled by user {}", id, currentUserId);
        return IrrigationEventResponse.from(saved);
    }
}
