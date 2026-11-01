package com.aquaai.intelligence.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.intelligence.application.dto.AgronomicAssessmentResponse;
import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.port.AgronomicAssessmentRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFieldAgronomicAssessmentUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final AgronomicAssessmentRepository assessmentRepository;
    private final EvaluateFieldIntelligenceUseCase evaluateFieldIntelligenceUseCase;

    @Transactional
    public AgronomicAssessmentResponse execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        AgronomicAssessment assessment = assessmentRepository.findLatestByFieldId(fieldId)
                .orElseGet(() -> {
                    var evaluation = evaluateFieldIntelligenceUseCase.execute(fieldId);
                    return assessmentRepository.findLatestByFieldId(fieldId).orElse(null);
                });

        return AgronomicAssessmentResponse.fromDomain(assessment);
    }
}
