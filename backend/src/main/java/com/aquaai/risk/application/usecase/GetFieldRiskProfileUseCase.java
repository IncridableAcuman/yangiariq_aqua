package com.aquaai.risk.application.usecase;

import com.aquaai.risk.api.dto.FieldRiskProfileResponse;
import com.aquaai.risk.application.service.RiskAssessmentCoordinatorService;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFieldRiskProfileUseCase {

    private final RiskFieldPort fieldPort;
    private final RiskAssessmentRepository assessmentRepository;
    private final RiskAssessmentCoordinatorService coordinatorService;
    private final CurrentUserProvider currentUserProvider;

    public FieldRiskProfileResponse execute(UUID fieldId) {
        if (fieldId == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        UUID currentUserId = currentUserProvider.getCurrentUserId().orElse(null);
        UserRole currentRole = currentUserProvider.getCurrentUserRole().orElse(UserRole.FARMER);

        RiskFieldPort.FieldInfo field = fieldPort.getFieldInfo(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        // Enforce anti-IDOR check for FARMER: must be owner
        if (currentRole == UserRole.FARMER && (currentUserId == null || !field.ownerId().equals(currentUserId))) {
            throw new ResourceNotFoundException("Field", fieldId);
        }

        Optional<FarmRiskAssessment> latestOpt = assessmentRepository.findLatestByFieldId(fieldId);
        FarmRiskAssessment assessment;
        if (latestOpt.isEmpty() || Duration.between(latestOpt.get().getEvaluatedAt(), Instant.now()).toMinutes() >= 30) {
            assessment = coordinatorService.evaluateAndPersistFieldRisk(fieldId);
        } else {
            assessment = latestOpt.get();
        }

        return FieldRiskProfileResponse.fromDomain(
                assessment,
                field.areaHectares(),
                field.cropType(),
                field.soilType(),
                field.irrigationMethod()
        );
    }
}
