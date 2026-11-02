package com.aquaai.operations.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.operations.api.dto.StartOperationRequest;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Records manual operational commencement for an approved/scheduled smart irrigation plan.
 * Strictly decision-support & manual field logging; no autonomous valve control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartOperationalExecutionUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final IrrigationEventRepository eventRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, StartOperationRequest request) {
        return execute(
                planId,
                request,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, StartOperationRequest request, UUID currentUserId, UserRole currentRole) {
        SmartIrrigationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SmartIrrigationPlan", planId));

        fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);

        if (plan.getStatus() != IrrigationPlanStatus.SCHEDULED && plan.getStatus() != IrrigationPlanStatus.APPROVED) {
            throw new BusinessRuleException(String.format(
                    "Cannot start irrigation plan in status %s; expected SCHEDULED or APPROVED.", plan.getStatus()
            ));
        }

        Instant startedAt = request != null && request.getStartedAt() != null ?
                request.getStartedAt() : Instant.now();
        String notes = request != null && request.getOperatorNotes() != null ?
                request.getOperatorNotes() : "Manually started by operator " + currentUserId;

        // Advance plan aggregate state
        plan.start();
        SmartIrrigationPlan savedPlan = planRepository.save(plan);

        // Record real-time operational delivery event in water bounded context
        IrrigationEvent event = IrrigationEvent.start(
                plan.getFieldId(),
                null, // smart plan reference embedded in notes to decouple from legacy irrigation_plans FK
                startedAt,
                notes + " [SmartPlan: " + plan.getId() + "]"
        );
        eventRepository.save(event);

        log.info("Irrigation operation recorded STARTED for plan {} on field {}", planId, plan.getFieldId());
        return savedPlan;
    }
}
