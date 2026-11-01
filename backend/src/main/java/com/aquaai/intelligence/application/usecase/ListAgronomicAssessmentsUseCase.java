package com.aquaai.intelligence.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.intelligence.application.dto.AgronomicAssessmentResponse;
import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.port.AgronomicAssessmentRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListAgronomicAssessmentsUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final AgronomicAssessmentRepository assessmentRepository;

    @Transactional(readOnly = true)
    public PageResponse<AgronomicAssessmentResponse> execute(
            UUID fieldId,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        int boundedPage = Math.max(0, page);
        int boundedSize = Math.clamp(size, 1, 100);

        List<AgronomicAssessment> list = assessmentRepository.findByFieldId(fieldId, from, to, boundedPage, boundedSize);
        long totalElements = assessmentRepository.countByFieldId(fieldId, from, to);
        int totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / boundedSize) : 0;

        List<AgronomicAssessmentResponse> content = list.stream()
                .map(AgronomicAssessmentResponse::fromDomain)
                .toList();

        return PageResponse.<AgronomicAssessmentResponse>builder()
                .content(content)
                .page(boundedPage)
                .size(boundedSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(boundedPage == 0)
                .last(boundedPage >= totalPages - 1)
                .build();
    }
}
