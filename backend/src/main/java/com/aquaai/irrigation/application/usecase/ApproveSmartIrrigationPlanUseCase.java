package com.aquaai.irrigation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.ConstraintStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveSmartIrrigationPlanUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, boolean acknowledgeWarnings) {
        return execute(
                planId,
                acknowledgeWarnings,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, boolean acknowledgeWarnings, UUID currentUserId, UserRole currentRole) {
        SmartIrrigationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SmartIrrigationPlan", planId));

        // Enforce anti-IDOR parcel ownership (throws 404 if unauthorized farmer)
        fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);

        if (plan.isBlocked()) {
            throw new BusinessRuleException("Cannot approve plan blocked by safety interlock: " + plan.getBlockedReason());
        }

        // Check if any warnings exist requiring explicit acknowledgment
        boolean hasWarnings = plan.getConstraints().stream()
                .anyMatch(c -> c.getStatus() == ConstraintStatus.WARNING || c.getStatus() == ConstraintStatus.VIOLATED);

        if (hasWarnings && !acknowledgeWarnings) {
            throw new BusinessRuleException("This plan has active constraints or warnings that must be explicitly acknowledged prior to approval.");
        }

        plan.approve(currentUserId);
        log.info("Smart irrigation plan {} approved by user {}", plan.getId(), currentUserId);
        return planRepository.save(plan);
    }
}
