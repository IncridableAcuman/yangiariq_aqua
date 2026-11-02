package com.aquaai.operations.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.PlanConstraint;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.operations.api.dto.ScheduleConflictDto;
import com.aquaai.operations.api.dto.ScheduleOperationalPlanRequest;
import com.aquaai.operations.application.service.ScheduleConflictDetectionService;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Dispatches an approved smart irrigation plan into the operational schedule.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleOperationalPlanUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final ScheduleConflictDetectionService conflictDetectionService;
    private final WaterBudgetDataPort waterBudgetDataPort;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, ScheduleOperationalPlanRequest request) {
        return execute(
                planId,
                request,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, ScheduleOperationalPlanRequest request, UUID currentUserId, UserRole currentRole) {
        SmartIrrigationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SmartIrrigationPlan", planId));

        fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);

        if (plan.getStatus() != IrrigationPlanStatus.APPROVED && plan.getStatus() != IrrigationPlanStatus.SCHEDULED) {
            throw new BusinessRuleException(String.format(
                    "Cannot schedule plan in status %s; plan must be APPROVED.", plan.getStatus()
            ));
        }

        Instant start = request != null && request.getScheduledStartAt() != null ?
                request.getScheduledStartAt() : plan.getPlannedStartAt();
        Instant end = request != null && request.getScheduledEndAt() != null ?
                request.getScheduledEndAt() : plan.getPlannedEndAt();

        // Detect conflicts with other scheduled turns on this parcel
        ScheduleConflictDto conflict = conflictDetectionService.detectConflict(
                plan.getFieldId(),
                plan.getId(),
                start,
                end,
                plan.getRecommendedVolumeM3(),
                waterBudgetDataPort.getWaterBudgetForField(plan.getFieldId()).orElse(null),
                false,
                false
        );

        if (conflict.isHasConflict()) {
            throw new BusinessRuleException(String.format(
                    "Scheduling conflict detected: %s. %s",
                    conflict.getConflictMessage(), conflict.getSuggestedAdjustment()
            ));
        }

        if (plan.getStatus() == IrrigationPlanStatus.APPROVED) {
            plan.schedule();
        }

        log.info("Smart irrigation plan {} successfully scheduled for start={}", planId, start);
        return planRepository.save(plan);
    }
}
