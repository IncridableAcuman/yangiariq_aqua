package com.aquaai.operations.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.operations.api.dto.RejectOperationalPlanRequest;
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
import java.util.UUID;

/**
 * Rejects/cancels a proposed smart irrigation plan with an explanatory reason.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RejectOperationalPlanUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final RecommendationRepository recommendationRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, RejectOperationalPlanRequest request) {
        return execute(
                planId,
                request,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID planId, RejectOperationalPlanRequest request, UUID currentUserId, UserRole currentRole) {
        SmartIrrigationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SmartIrrigationPlan", planId));

        fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);

        if (request == null || request.getReason() == null || request.getReason().trim().isBlank()) {
            throw new BusinessRuleException("Rejection reason must be provided.");
        }

        plan.cancel(request.getReason());
        SmartIrrigationPlan savedPlan = planRepository.save(plan);
        log.info("Smart irrigation plan {} rejected by user {}: {}", planId, currentUserId, request.getReason());

        // Dismiss active recommendations for this field
        try {
            List<AgronomicRecommendation> activeRecs = recommendationRepository.findActiveByFieldId(plan.getFieldId());
            for (AgronomicRecommendation rec : activeRecs) {
                rec.dismiss(currentUserId, "Plan rejected: " + request.getReason());
                recommendationRepository.save(rec);
            }
        } catch (Exception e) {
            log.warn("Could not dismiss active recommendations for field {}: {}", plan.getFieldId(), e.getMessage());
        }

        return savedPlan;
    }
}
