package com.aquaai.irrigation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteSmartIrrigationPlanUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, BigDecimal actualVolumeDeliveredM3) {
        return execute(
                planId,
                actualVolumeDeliveredM3,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, BigDecimal actualVolumeDeliveredM3, UUID currentUserId, UserRole currentRole) {
        SmartIrrigationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SmartIrrigationPlan", planId));

        // Enforce anti-IDOR parcel ownership (throws 404 if unauthorized farmer)
        fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);

        // If currently APPROVED or SCHEDULED, advance through IN_PROGRESS to COMPLETED
        if (plan.getStatus() == com.aquaai.irrigation.domain.model.IrrigationPlanStatus.APPROVED
                || plan.getStatus() == com.aquaai.irrigation.domain.model.IrrigationPlanStatus.SCHEDULED) {
            plan.start();
        }

        plan.complete(actualVolumeDeliveredM3 != null ? actualVolumeDeliveredM3 : plan.getRecommendedVolumeM3());
        log.info("Smart irrigation plan {} marked COMPLETED by user {}", plan.getId(), currentUserId);
        return planRepository.save(plan);
    }
}
