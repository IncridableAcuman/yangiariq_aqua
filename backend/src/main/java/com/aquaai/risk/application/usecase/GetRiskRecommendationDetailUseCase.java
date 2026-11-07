package com.aquaai.risk.application.usecase;

import com.aquaai.risk.api.dto.RiskRecommendationResponse;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskRecommendation;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRiskRecommendationDetailUseCase {

    private final RiskFieldPort fieldPort;
    private final RiskAssessmentRepository assessmentRepository;
    private final CurrentUserProvider currentUserProvider;

    public RiskRecommendationResponse execute(UUID recommendationId) {
        if (recommendationId == null) {
            throw new ResourceNotFoundException("RiskRecommendation", "null");
        }

        UUID currentUserId = currentUserProvider.getCurrentUserId().orElse(null);
        UserRole currentRole = currentUserProvider.getCurrentUserRole().orElse(UserRole.FARMER);
        boolean isDistrictScope = (currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR);

        List<UUID> accessibleIds = fieldPort.getAccessibleFieldIds(currentUserId, isDistrictScope);
        if (accessibleIds.isEmpty()) {
            throw new ResourceNotFoundException("RiskRecommendation", recommendationId);
        }

        List<FarmRiskAssessment> latest = assessmentRepository.findAllLatestByFieldIds(accessibleIds);
        for (FarmRiskAssessment a : latest) {
            if (a.getRecommendations() != null) {
                for (RiskRecommendation r : a.getRecommendations()) {
                    if (r.id().equals(recommendationId)) {
                        return RiskRecommendationResponse.fromDomain(r);
                    }
                }
            }
        }

        throw new ResourceNotFoundException("RiskRecommendation", recommendationId);
    }
}
