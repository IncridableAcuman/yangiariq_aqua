package com.aquaai.operations.application.usecase;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.irrigation.domain.service.HydraulicSafetyInterlock;
import com.aquaai.operations.api.dto.ApproveOperationalPlanRequest;
import com.aquaai.operations.api.dto.ScheduleConflictDto;
import com.aquaai.operations.application.service.ScheduleConflictDetectionService;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Executes human approval with strict server-side revalidation of authoritative state:
 * - Hydraulic safety interlock
 * - Real-time volumetric water budget
 * - Mandatory operational constraint acknowledgement
 * - Schedule conflict detection
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveOperationalPlanUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final AnomalyRepository anomalyRepository;
    private final WaterBudgetDataPort waterBudgetDataPort;
    private final ScheduleConflictDetectionService conflictDetectionService;
    private final RecommendationRepository recommendationRepository;
    private final CurrentUserProvider currentUserProvider;

    private final HydraulicSafetyInterlock safetyInterlock = new HydraulicSafetyInterlock();

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, ApproveOperationalPlanRequest request) {
        return execute(
                planId,
                request,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, ApproveOperationalPlanRequest request, UUID currentUserId, UserRole currentRole) {
        SmartIrrigationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SmartIrrigationPlan", planId));

        // 1. Enforce anti-IDOR parcel tenancy (throws 404 if unauthorized farmer)
        fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);

        // 2. Enforce Mandatory Human Acknowledgement Checkbox
        if (request == null || !request.isAcknowledgeConstraints()) {
            throw new BusinessRuleException("Mandatory acknowledgement required: You must review and explicitly acknowledge operational constraints prior to approval.");
        }

        // 3. Plan Status Invariant
        if (!plan.getStatus().canBeApproved()) {
            throw new BusinessRuleException(String.format(
                    "Cannot approve plan in status %s; expected DRAFT or PROPOSED.", plan.getStatus()
            ));
        }

        // 4. Server-Side Revalidation: Hydraulic Safety Interlock
        List<Anomaly> activeAnomalies = anomalyRepository.findActiveByFieldIdIn(List.of(plan.getFieldId()));
        boolean hasSuspectedPipeBurst = activeAnomalies.stream()
                .anyMatch(a -> a.getAnomalyType() == AnomalyType.SUSPECTED_PIPE_BURST);
        boolean hasCriticalPressureDrop = activeAnomalies.stream()
                .anyMatch(a -> a.getAnomalyType() == AnomalyType.WATER_PRESSURE_DROP && a.getSeverity() == AnomalySeverity.CRITICAL);

        HydraulicSafetyInterlock.InterlockResult safetyResult = safetyInterlock.evaluate(
                hasSuspectedPipeBurst,
                hasCriticalPressureDrop,
                null,
                null
        );

        if (safetyResult.isBlocked() || plan.isBlocked()) {
            String reason = safetyResult.blockedReason() != null ? safetyResult.blockedReason() : plan.getBlockedReason();
            throw new BusinessRuleException("Approval rejected by Hydraulic Safety Interlock: " + reason);
        }

        // 5. Server-Side Revalidation: Real-Time Water Budget Quota
        Optional<WaterBudget> budgetOpt = waterBudgetDataPort.getWaterBudgetForField(plan.getFieldId());
        WaterBudget budget = budgetOpt.orElse(null);
        if (budget != null && !budget.canFulfill(plan.getRecommendedVolumeM3())) {
            throw new BusinessRuleException(String.format(
                    "Approval could not be completed. Reason: Water budget changed since this plan was created. Available: %s m³, Required: %s m³",
                    budget.getRemainingAvailableM3(), plan.getRecommendedVolumeM3()
            ));
        }

        // 6. Server-Side Revalidation: Schedule Conflicts
        ScheduleConflictDto conflict = conflictDetectionService.detectConflict(
                plan.getFieldId(),
                plan.getId(),
                plan.getPlannedStartAt(),
                plan.getPlannedEndAt(),
                plan.getRecommendedVolumeM3(),
                budget,
                hasSuspectedPipeBurst,
                hasCriticalPressureDrop
        );

        if (conflict.isHasConflict()) {
            throw new BusinessRuleException(String.format(
                    "Approval could not be completed. Reason: %s Suggested adjustment: %s",
                    conflict.getConflictMessage(), conflict.getSuggestedAdjustment()
            ));
        }

        // 7. Advance Plan Aggregate State
        plan.approve(currentUserId);
        SmartIrrigationPlan savedPlan = planRepository.save(plan);
        log.info("Smart irrigation plan {} successfully approved by user {}", planId, currentUserId);

        // 8. Synchronize Active Recommendation State to ADOPTED
        try {
            List<AgronomicRecommendation> activeRecs = recommendationRepository.findActiveByFieldId(plan.getFieldId());
            for (AgronomicRecommendation rec : activeRecs) {
                rec.accept(currentUserId);
                recommendationRepository.save(rec);
            }
        } catch (Exception e) {
            log.warn("Could not transition active recommendations for field {}: {}", plan.getFieldId(), e.getMessage());
        }

        return savedPlan;
    }
}
