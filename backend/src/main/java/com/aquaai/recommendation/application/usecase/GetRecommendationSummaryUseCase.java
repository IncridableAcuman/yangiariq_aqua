package com.aquaai.recommendation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.recommendation.domain.model.RecommendationSummaryData;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRecommendationSummaryUseCase {

    private final RecommendationRepository recommendationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public RecommendationSummaryData execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        if (fieldId != null) {
            fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);
        }

        return recommendationRepository.getSummary(fieldId);
    }
}
