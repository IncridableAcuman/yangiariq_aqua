package com.aquaai.risk.application.usecase;

import com.aquaai.risk.api.dto.FieldRiskProfileResponse;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResolveRiskAssessmentUseCase {

    private final RiskAssessmentRepository assessmentRepository;
    private final RiskFieldPort fieldPort;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FieldRiskProfileResponse execute(UUID assessmentId, String resolutionNotes) {
        if (assessmentId == null) {
            throw new ResourceNotFoundException("RiskAssessment", "null");
        }

        FarmRiskAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskAssessment", assessmentId));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        RiskFieldPort.FieldInfo field = fieldPort.getFieldInfo(assessment.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", assessment.getFieldId()));

        if (currentRole == UserRole.FARMER && !field.ownerId().equals(currentUserId)) {
            throw new ResourceNotFoundException("RiskAssessment", assessmentId);
        }

        assessment.resolve(currentUserId, resolutionNotes);
        FarmRiskAssessment saved = assessmentRepository.save(assessment);

        return FieldRiskProfileResponse.fromDomain(
                saved,
                field.areaHectares(),
                field.cropType(),
                field.soilType(),
                field.irrigationMethod()
        );
    }
}
