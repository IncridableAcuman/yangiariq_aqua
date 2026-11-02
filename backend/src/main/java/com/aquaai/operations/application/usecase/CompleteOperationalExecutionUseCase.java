package com.aquaai.operations.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.operations.api.dto.CompleteOperationRequest;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Records manual operational completion and actual delivered volumetric usage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteOperationalExecutionUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final IrrigationEventRepository eventRepository;
    private final RecommendationRepository recommendationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, CompleteOperationRequest request) {
        return execute(
                planId,
                request,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, CompleteOperationRequest request, UUID currentUserId, UserRole currentRole) {
        SmartIrrigationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SmartIrrigationPlan", planId));

        fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);

        // If currently APPROVED or SCHEDULED, advance through IN_PROGRESS to COMPLETED
        if (plan.getStatus() == IrrigationPlanStatus.APPROVED || plan.getStatus() == IrrigationPlanStatus.SCHEDULED) {
            plan.start();
        }

        if (plan.getStatus() != IrrigationPlanStatus.IN_PROGRESS) {
            throw new BusinessRuleException(String.format(
                    "Cannot complete irrigation plan in status %s; expected IN_PROGRESS.", plan.getStatus()
            ));
        }

        BigDecimal actualVolume = request != null && request.getActualVolumeDeliveredM3() != null ?
                request.getActualVolumeDeliveredM3() : plan.getRecommendedVolumeM3();
        Instant endedAt = request != null && request.getEndedAt() != null ?
                request.getEndedAt() : Instant.now();
        String notes = request != null && request.getOperatorNotes() != null ?
                request.getOperatorNotes() : "Completed manually by operator " + currentUserId;

        // Advance plan aggregate state
        plan.complete(actualVolume);
        SmartIrrigationPlan savedPlan = planRepository.save(plan);

        // Update active irrigation event in water bounded context
        List<IrrigationEvent> events = eventRepository.findByFieldId(plan.getFieldId());
        IrrigationEvent activeEvent = events.stream()
                .filter(e -> e.getStatus() == IrrigationEventStatus.STARTED)
                .findFirst()
                .orElse(null);

        if (activeEvent != null) {
            activeEvent.complete(endedAt, actualVolume, notes);
            eventRepository.save(activeEvent);
        } else {
            // Log completed historical record if start was not recorded prior
            Instant startedAt = plan.getPlannedStartAt() != null ? plan.getPlannedStartAt() : endedAt.minusSeconds(7200);
            IrrigationEvent completedRecord = IrrigationEvent.recordCompleted(
                    plan.getFieldId(),
                    null,
                    startedAt,
                    endedAt,
                    actualVolume,
                    notes + " [SmartPlan: " + plan.getId() + "]"
            );
            eventRepository.save(completedRecord);
        }

        // Complete active recommendations for this field
        try {
            List<AgronomicRecommendation> activeRecs = recommendationRepository.findActiveByFieldId(plan.getFieldId());
            for (AgronomicRecommendation rec : activeRecs) {
                rec.complete(currentUserId);
                recommendationRepository.save(rec);
            }
        } catch (Exception e) {
            log.warn("Could not transition recommendations to COMPLETED for field {}: {}", plan.getFieldId(), e.getMessage());
        }

        log.info("Irrigation operation recorded COMPLETED for plan {} on field {} with volume {} m³",
                planId, plan.getFieldId(), actualVolume);
        return savedPlan;
    }
}
