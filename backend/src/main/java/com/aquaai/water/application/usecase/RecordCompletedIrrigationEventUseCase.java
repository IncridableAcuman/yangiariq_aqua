package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationEventResponse;
import com.aquaai.water.api.dto.RecordCompletedIrrigationEventRequest;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Directly logs an already-completed historical watering event.
 * Enforces ownership, plan consistency, and volume non-negativity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordCompletedIrrigationEventUseCase {

    private final IrrigationEventRepository irrigationEventRepository;
    private final IrrigationPlanRepository irrigationPlanRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IrrigationEventResponse execute(RecordCompletedIrrigationEventRequest request) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field ownership & anti-enumeration
        fieldOwnershipChecker.getFieldForMutation(request.fieldId(), currentUserId, currentRole);

        if (request.irrigationPlanId() != null) {
            IrrigationPlan plan = irrigationPlanRepository.findById(request.irrigationPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("IrrigationPlan", request.irrigationPlanId()));

            if (!plan.getFieldId().equals(request.fieldId())) {
                throw new BusinessRuleException("Irrigation plan " + request.irrigationPlanId() + " does not belong to field " + request.fieldId());
            }
        }

        IrrigationEvent event = IrrigationEvent.recordCompleted(
                request.fieldId(),
                request.irrigationPlanId(),
                request.startedAt(),
                request.endedAt(),
                request.actualVolumeM3(),
                request.notes()
        );

        IrrigationEvent saved = irrigationEventRepository.save(event);
        log.info("Completed irrigation event {} recorded for field {} by user {}", saved.getId(), saved.getFieldId(), currentUserId);
        return IrrigationEventResponse.from(saved);
    }
}
