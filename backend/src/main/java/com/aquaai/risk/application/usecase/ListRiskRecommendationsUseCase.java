package com.aquaai.risk.application.usecase;

import com.aquaai.risk.api.dto.RiskRecommendationResponse;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskRecommendation;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListRiskRecommendationsUseCase {

    private final RiskFieldPort fieldPort;
    private final RiskAssessmentRepository assessmentRepository;
    private final CurrentUserProvider currentUserProvider;

    public PageResponse<RiskRecommendationResponse> execute(
            String severity,
            String category,
            UUID fieldId,
            int page,
            int size
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId().orElse(null);
        UserRole currentRole = currentUserProvider.getCurrentUserRole().orElse(UserRole.FARMER);
        boolean isDistrictScope = (currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR);

        List<UUID> accessibleIds;
        if (fieldId != null) {
            RiskFieldPort.FieldInfo field = fieldPort.getFieldInfo(fieldId)
                    .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

            if (currentRole == UserRole.FARMER && (currentUserId == null || !field.ownerId().equals(currentUserId))) {
                throw new ResourceNotFoundException("Field", fieldId);
            }
            accessibleIds = List.of(fieldId);
        } else {
            accessibleIds = fieldPort.getAccessibleFieldIds(currentUserId, isDistrictScope);
        }

        if (accessibleIds.isEmpty()) {
            return PageResponse.<RiskRecommendationResponse>builder()
                    .content(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();
        }

        List<FarmRiskAssessment> latest = assessmentRepository.findAllLatestByFieldIds(accessibleIds);
        List<RiskRecommendation> allRecs = new ArrayList<>();
        for (FarmRiskAssessment a : latest) {
            if (a.getRecommendations() != null) {
                allRecs.addAll(a.getRecommendations());
            }
        }

        List<RiskRecommendationResponse> filtered = allRecs.stream()
                .filter(r -> {
                    if (severity != null && !severity.isBlank() && !severity.equalsIgnoreCase("ALL")) {
                        if (!r.severity().name().equalsIgnoreCase(severity)) return false;
                    }
                    if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
                        if (!r.category().name().equalsIgnoreCase(category)) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparingInt(RiskRecommendation::priorityRank)
                        .thenComparing(RiskRecommendation::createdAt, Comparator.reverseOrder()))
                .map(RiskRecommendationResponse::fromDomain)
                .toList();

        int totalElements = filtered.size();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        int fromIndex = safePage * safeSize;
        if (fromIndex >= totalElements) {
            return PageResponse.<RiskRecommendationResponse>builder()
                    .content(Collections.emptyList())
                    .page(safePage)
                    .size(safeSize)
                    .totalElements(totalElements)
                    .totalPages((int) Math.ceil((double) totalElements / safeSize))
                    .first(safePage == 0)
                    .last(true)
                    .build();
        }
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<RiskRecommendationResponse> paged = filtered.subList(fromIndex, toIndex);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        return PageResponse.<RiskRecommendationResponse>builder()
                .content(paged)
                .page(safePage)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(safePage == 0)
                .last(safePage >= totalPages - 1)
                .build();
    }
}
