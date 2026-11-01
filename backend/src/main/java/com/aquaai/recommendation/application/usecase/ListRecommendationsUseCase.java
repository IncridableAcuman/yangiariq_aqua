package com.aquaai.recommendation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListRecommendationsUseCase {

    private final RecommendationRepository recommendationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final FieldRepository fieldRepository;
    private final CurrentUserProvider currentUserProvider;

    public record Query(
            UUID fieldId,
            RecommendationStatus status,
            RecommendationPriority priority,
            RecommendationType type,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
    }

    public record Result(
            List<AgronomicRecommendation> content,
            long totalElements,
            int page,
            int size
    ) {
    }

    @Transactional(readOnly = true)
    public Result execute(Query query) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // 1. Farmer Anti-IDOR Enforcement
        if (currentRole == UserRole.FARMER) {
            if (query.fieldId() != null) {
                // Farmer specified fieldId: verify ownership, throw 404 if not owned
                fieldOwnershipChecker.getFieldForRead(query.fieldId(), currentUserId, currentRole);
            } else {
                // Farmer did not specify fieldId: find all fields belonging to this farmer
                List<Field> farmerFields = fieldRepository.findByOwnerId(currentUserId);
                if (farmerFields.isEmpty()) {
                    return new Result(Collections.emptyList(), 0, query.page(), query.size());
                }

                Set<UUID> farmerFieldIds = farmerFields.stream().map(Field::getId).collect(Collectors.toSet());
                List<AgronomicRecommendation> allRecs = recommendationRepository.findAll(
                        null, query.status(), query.priority(), query.type(), query.from(), query.to(),
                        0, 500
                );

                List<AgronomicRecommendation> filtered = allRecs.stream()
                        .filter(r -> farmerFieldIds.contains(r.getFieldId()))
                        .toList();

                int fromIndex = Math.min(query.page() * query.size(), filtered.size());
                int toIndex = Math.min(fromIndex + query.size(), filtered.size());
                List<AgronomicRecommendation> paged = filtered.subList(fromIndex, toIndex);

                return new Result(paged, filtered.size(), query.page(), query.size());
            }
        }

        // Operator / Admin or Farmer with verified single field
        List<AgronomicRecommendation> content = recommendationRepository.findAll(
                query.fieldId(), query.status(), query.priority(), query.type(), query.from(), query.to(),
                query.page(), query.size()
        );

        long total = recommendationRepository.countAll(
                query.fieldId(), query.status(), query.priority(), query.type(), query.from(), query.to()
        );

        return new Result(content, total, query.page(), query.size());
    }
}
