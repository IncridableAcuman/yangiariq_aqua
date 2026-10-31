package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CompleteIrrigationEventRequest;
import com.aquaai.water.api.dto.IrrigationEventResponse;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Completes an in-progress irrigation delivery operation by recording finish time and consumed volume.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteIrrigationEventUseCase {

    private final IrrigationEventRepository irrigationEventRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IrrigationEventResponse execute(UUID id, CompleteIrrigationEventRequest request) {
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

        event.complete(request.endedAt(), request.actualVolumeM3(), request.notes());
        IrrigationEvent saved = irrigationEventRepository.save(event);

        log.info("Irrigation event {} completed with volume {} m3 by user {}", id, request.actualVolumeM3(), currentUserId);
        return IrrigationEventResponse.from(saved);
    }
}
