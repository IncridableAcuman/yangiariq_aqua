package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.WaterAllocationResponse;
import com.aquaai.water.domain.model.WaterAllocationPage;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Use case: Lists water allocation quotas with filtering and pagination.
 * Restricts FARMER queries strictly to parcels owned by the authenticated caller.
 */
@Service
@RequiredArgsConstructor
public class ListWaterAllocationsUseCase {

    private final WaterAllocationRepository waterAllocationRepository;
    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public PageResponse<WaterAllocationResponse> execute(
            UUID fieldId,
            UUID waterSourceId,
            WaterAllocationStatus status,
            Instant periodStart,
            Instant periodEnd,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        Collection<UUID> effectiveFieldIds;

        if (currentRole == UserRole.FARMER) {
            if (fieldId != null) {
                // Verify caller owns this specific field (404 anti-enumeration)
                fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);
                effectiveFieldIds = List.of(fieldId);
            } else {
                // Filter by all fields owned by this farmer
                List<UUID> farmerFieldIds = fieldRepository.findByOwnerId(currentUserId).stream()
                        .map(Field::getId)
                        .toList();

                if (farmerFieldIds.isEmpty()) {
                    return PageResponse.<WaterAllocationResponse>builder()
                            .content(Collections.emptyList())
                            .page(Math.max(page, 0))
                            .size((size <= 0) ? 20 : Math.min(size, 100))
                            .totalElements(0)
                            .totalPages(0)
                            .first(true)
                            .last(true)
                            .build();
                }
                effectiveFieldIds = farmerFieldIds;
            }
        } else {
            // OPERATOR and ADMIN can filter by specific field or list district-wide
            if (fieldId != null) {
                fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);
                effectiveFieldIds = List.of(fieldId);
            } else {
                effectiveFieldIds = Collections.emptyList();
            }
        }

        WaterAllocationPage allocationPage = waterAllocationRepository.findWithFilters(
                effectiveFieldIds,
                waterSourceId,
                status,
                periodStart,
                periodEnd,
                page,
                size,
                sortBy,
                sortDirection
        );

        List<WaterAllocationResponse> responses = allocationPage.content().stream()
                .map(WaterAllocationResponse::from)
                .toList();

        return PageResponse.<WaterAllocationResponse>builder()
                .content(responses)
                .page(allocationPage.page())
                .size(allocationPage.size())
                .totalElements(allocationPage.totalElements())
                .totalPages(allocationPage.totalPages())
                .first(allocationPage.isFirst())
                .last(allocationPage.isLast())
                .build();
    }
}
