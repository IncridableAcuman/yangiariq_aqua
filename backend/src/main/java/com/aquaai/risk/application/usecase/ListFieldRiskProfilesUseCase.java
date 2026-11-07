package com.aquaai.risk.application.usecase;

import com.aquaai.risk.api.dto.FieldRiskProfileResponse;
import com.aquaai.risk.application.service.RiskAssessmentCoordinatorService;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListFieldRiskProfilesUseCase {

    private final RiskFieldPort fieldPort;
    private final RiskAssessmentRepository assessmentRepository;
    private final RiskAssessmentCoordinatorService coordinatorService;
    private final CurrentUserProvider currentUserProvider;

    public PageResponse<FieldRiskProfileResponse> execute(
            String severity,
            String category,
            String confidence,
            String status,
            String search,
            String sortBy,
            String sortDirection,
            int page,
            int size
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId().orElse(null);
        UserRole currentRole = currentUserProvider.getCurrentUserRole().orElse(UserRole.FARMER);
        boolean isDistrictScope = (currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR);

        List<RiskFieldPort.FieldInfo> fields = fieldPort.getAllAccessibleFieldInfos(currentUserId, isDistrictScope);
        if (fields.isEmpty()) {
            return PageResponse.<FieldRiskProfileResponse>builder()
                    .content(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();
        }

        List<UUID> fieldIds = fields.stream().map(RiskFieldPort.FieldInfo::id).toList();
        List<FarmRiskAssessment> latestList = assessmentRepository.findAllLatestByFieldIds(fieldIds);

        Map<UUID, FarmRiskAssessment> assessmentMap = new HashMap<>();
        for (FarmRiskAssessment a : latestList) {
            assessmentMap.put(a.getFieldId(), a);
        }

        Instant now = Instant.now();
        List<FieldRiskProfileResponse> profiles = new ArrayList<>();
        for (RiskFieldPort.FieldInfo f : fields) {
            FarmRiskAssessment a = assessmentMap.get(f.id());
            if (a == null || Duration.between(a.getEvaluatedAt(), now).toMinutes() >= 30) {
                try {
                    a = coordinatorService.evaluateAndPersistFieldRisk(f.id());
                } catch (Exception ignored) {}
            }
            if (a != null) {
                profiles.add(FieldRiskProfileResponse.fromDomain(
                        a,
                        f.areaHectares(),
                        f.cropType(),
                        f.soilType(),
                        f.irrigationMethod()
                ));
            }
        }

        // Apply filters
        List<FieldRiskProfileResponse> filtered = profiles.stream()
                .filter(p -> {
                    if (severity != null && !severity.isBlank() && !severity.equalsIgnoreCase("ALL")) {
                        if (!p.overallSeverity().name().equalsIgnoreCase(severity)) return false;
                    }
                    if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
                        if (!p.primaryRiskCategory().name().equalsIgnoreCase(category)) return false;
                    }
                    if (confidence != null && !confidence.isBlank() && !confidence.equalsIgnoreCase("ALL")) {
                        if (!p.overallConfidence().name().equalsIgnoreCase(confidence)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
                        if (!p.lifecycleStatus().name().equalsIgnoreCase(status)) return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String query = search.toLowerCase();
                        boolean matchName = p.fieldName() != null && p.fieldName().toLowerCase().contains(query);
                        boolean matchCrop = p.cropType() != null && p.cropType().toLowerCase().contains(query);
                        boolean matchReason = p.priorityReason() != null && p.priorityReason().toLowerCase().contains(query);
                        if (!matchName && !matchCrop && !matchReason) return false;
                    }
                    return true;
                })
                .toList();

        // Sort
        boolean asc = "asc".equalsIgnoreCase(sortDirection);
        Comparator<FieldRiskProfileResponse> comparator;
        if ("severity".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparingInt(p -> p.overallSeverity().getRank());
        } else if ("riskScore".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(FieldRiskProfileResponse::riskScore);
        } else if ("evaluatedAt".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(FieldRiskProfileResponse::evaluatedAt);
        } else {
            // Default: priority rank (1 is highest), then riskScore desc
            comparator = Comparator.comparingInt(FieldRiskProfileResponse::priorityRank)
                    .thenComparing(FieldRiskProfileResponse::riskScore, Comparator.reverseOrder());
        }

        if (!asc && !"priority".equalsIgnoreCase(sortBy) && sortBy != null) {
            comparator = comparator.reversed();
        }

        List<FieldRiskProfileResponse> sorted = new ArrayList<>(filtered);
        sorted.sort(comparator);

        int totalElements = sorted.size();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        int fromIndex = safePage * safeSize;
        if (fromIndex >= totalElements) {
            return PageResponse.<FieldRiskProfileResponse>builder()
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
        List<FieldRiskProfileResponse> paged = sorted.subList(fromIndex, toIndex);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        return PageResponse.<FieldRiskProfileResponse>builder()
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
