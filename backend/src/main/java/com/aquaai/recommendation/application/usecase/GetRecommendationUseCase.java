package com.aquaai.recommendation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRecommendationUseCase {

    private final RecommendationRepository recommendationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public AgronomicRecommendation execute(UUID recommendationId) {
        if (recommendationId == null) {
            throw new ResourceNotFoundException("Recommendation", "null");
        }

        AgronomicRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", recommendationId));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Anti-IDOR: Check that farmer owns the field of this recommendation
        try {
            fieldOwnershipChecker.getFieldForRead(recommendation.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Recommendation", recommendationId);
        }

        return recommendation;
    }
}
