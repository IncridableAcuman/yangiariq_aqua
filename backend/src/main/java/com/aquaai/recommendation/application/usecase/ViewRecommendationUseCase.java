package com.aquaai.recommendation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
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
public class ViewRecommendationUseCase {

    private final RecommendationRepository recommendationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public AgronomicRecommendation execute(UUID recommendationId) {
        if (recommendationId == null) {
            throw new ResourceNotFoundException("Recommendation", "null");
        }

        AgronomicRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", recommendationId));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        try {
            fieldOwnershipChecker.getFieldForRead(recommendation.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Recommendation", recommendationId);
        }

        recommendation.markViewed(currentUserId);
        AgronomicRecommendation saved = recommendationRepository.save(recommendation);
        log.info("Recommendation marked viewed: id={}, fieldId={}, by={}", saved.getId(), saved.getFieldId(), currentUserId);
        return saved;
    }
}
