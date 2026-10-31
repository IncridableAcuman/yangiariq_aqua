package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CreateIrrigationEventRequest;
import com.aquaai.water.api.dto.IrrigationEventResponse;
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
 * Use case: Initiates a real-time active irrigation delivery operation on an agricultural parcel.
 * Enforces parcel ownership and plan alignment invariants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartIrrigationEventUseCase {

    private final IrrigationEventRepository irrigationEventRepository;
    private final IrrigationPlanRepository irrigationPlanRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IrrigationEventResponse execute(CreateIrrigationEventRequest request) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field ownership & anti-enumeration
        fieldOwnershipChecker.getFieldForMutation(request.fieldId(), currentUserId, currentRole);

        // Cross-aggregate alignment if linked to a scheduled plan
        if (request.irrigationPlanId() != null) {
            IrrigationPlan plan = irrigationPlanRepository.findById(request.irrigationPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("IrrigationPlan", request.irrigationPlanId()));

            if (!plan.getFieldId().equals(request.fieldId())) {
                throw new BusinessRuleException("Irrigation plan " + request.irrigationPlanId() + " does not belong to field " + request.fieldId());
            }
        }

        IrrigationEvent event = IrrigationEvent.start(
                request.fieldId(),
                request.irrigationPlanId(),
                request.startedAt(),
                request.notes()
        );

        IrrigationEvent saved = irrigationEventRepository.save(event);
        log.info("Irrigation event {} started for field {} by user {}", saved.getId(), saved.getFieldId(), currentUserId);
        return IrrigationEventResponse.from(saved);
    }
}
