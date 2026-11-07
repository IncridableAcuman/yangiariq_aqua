package com.aquaai.risk.application.usecase;

import com.aquaai.risk.api.dto.FieldRiskProfileResponse;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFieldRiskHistoryUseCase {

    private final RiskFieldPort fieldPort;
    private final RiskAssessmentRepository assessmentRepository;
    private final CurrentUserProvider currentUserProvider;

    public PageResponse<FieldRiskProfileResponse> execute(
            UUID fieldId,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        if (fieldId == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        UUID currentUserId = currentUserProvider.getCurrentUserId().orElse(null);
        UserRole currentRole = currentUserProvider.getCurrentUserRole().orElse(UserRole.FARMER);

        RiskFieldPort.FieldInfo field = fieldPort.getFieldInfo(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        if (currentRole == UserRole.FARMER && (currentUserId == null || !field.ownerId().equals(currentUserId))) {
            throw new ResourceNotFoundException("Field", fieldId);
        }

        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));

        List<FarmRiskAssessment> historical = assessmentRepository.findByFieldIdHistory(fieldId, from, to, safePage, safeSize);
        long totalElements = assessmentRepository.countHistoricalByFieldId(fieldId, from, to);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        List<FieldRiskProfileResponse> content = historical.stream()
                .map(a -> FieldRiskProfileResponse.fromDomain(
                        a,
                        field.areaHectares(),
                        field.cropType(),
                        field.soilType(),
                        field.irrigationMethod()
                ))
                .toList();

        return PageResponse.<FieldRiskProfileResponse>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(safePage == 0)
                .last(safePage >= totalPages - 1)
                .build();
    }
}
