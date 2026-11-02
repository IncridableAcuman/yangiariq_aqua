package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.EvaluationHistoryResponse;
import com.aquaai.prediction.application.dto.PredictionEvaluationDetailDto;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPredictionEvaluationHistoryUseCase {

    private final PredictionEvaluationRepository evaluationRepository;
    private final PredictionDataProvider dataProvider;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public EvaluationHistoryResponse execute(
            UUID targetFieldId,
            String typeStr,
            String statusStr,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(targetFieldId, typeStr, statusStr, from, to, page, size, currentUserId, currentRole);
    }

    @Transactional(readOnly = true)
    public EvaluationHistoryResponse execute(
            UUID targetFieldId,
            String typeStr,
            String statusStr,
            Instant from,
            Instant to,
            int page,
            int size,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> accessibleFields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);
        Map<UUID, String> fieldNameMap = accessibleFields.stream().collect(Collectors.toMap(FieldMetadata::fieldId, FieldMetadata::name, (a, b) -> a));

        Collection<UUID> targetFieldIds;
        if (targetFieldId != null) {
            // Anti-IDOR check
            FieldMetadata singleField = accessibleFields.stream()
                    .filter(f -> f.fieldId().equals(targetFieldId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Field", targetFieldId));
            targetFieldIds = List.of(singleField.fieldId());
        } else {
            targetFieldIds = accessibleFields.stream().map(FieldMetadata::fieldId).toList();
        }

        PredictionType type = null;
        if (typeStr != null && !typeStr.isBlank()) {
            try {
                type = PredictionType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        EvaluationStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = EvaluationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));

        List<PredictionEvaluation> evals = evaluationRepository.findEvaluations(
                targetFieldIds, type, status, from, to, safePage, safeSize);
        long total = evaluationRepository.countEvaluations(targetFieldIds, type, status, from, to);
        int totalPages = (int) Math.ceil((double) total / safeSize);

        List<PredictionEvaluationDetailDto> content = evals.stream()
                .map(e -> PredictionEvaluationDetailDto.fromDomain(e, fieldNameMap.get(e.getFieldId())))
                .toList();

        return EvaluationHistoryResponse.builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }
}
