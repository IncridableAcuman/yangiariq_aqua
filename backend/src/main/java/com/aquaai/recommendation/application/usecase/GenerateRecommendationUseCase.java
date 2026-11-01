package com.aquaai.recommendation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.recommendation.application.service.RecommendationOrchestrationService;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * On-demand generation use case for triggering evaluation of agronomic recommendations.
 * Enforces farmer ownership anti-IDOR check.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateRecommendationUseCase {

    private final RecommendationOrchestrationService orchestrationService;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public AgronomicRecommendation execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Anti-IDOR: Farmer must own the field to generate recommendations
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        log.info("Generating recommendation for field {} requested by user {} ({})", fieldId, currentUserId, currentRole);
        return orchestrationService.generateForField(fieldId);
    }
}
