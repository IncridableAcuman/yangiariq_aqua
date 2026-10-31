package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationPlanResponse;
import com.aquaai.water.domain.model.IrrigationPlanPage;
import com.aquaai.water.domain.model.IrrigationPlanStatus;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Use case: Lists scheduled irrigation plans with filtering and pagination.
 * Restricts FARMER queries strictly to parcels owned by the authenticated caller.
 */
@Service
@RequiredArgsConstructor
public class ListIrrigationPlansUseCase {

    private final IrrigationPlanRepository irrigationPlanRepository;
    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public PageResponse<IrrigationPlanResponse> execute(
            UUID fieldId,
            UUID waterAllocationId,
            IrrigationPlanStatus status,
            Instant startDate,
            Instant endDate,
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
                fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);
                effectiveFieldIds = List.of(fieldId);
            } else {
                List<UUID> farmerFieldIds = fieldRepository.findByOwnerId(currentUserId).stream()
                        .map(Field::getId)
                        .toList();

                if (farmerFieldIds.isEmpty()) {
                    return PageResponse.<IrrigationPlanResponse>builder()
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
            if (fieldId != null) {
                fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);
                effectiveFieldIds = List.of(fieldId);
            } else {
                effectiveFieldIds = Collections.emptyList();
            }
        }

        IrrigationPlanPage planPage = irrigationPlanRepository.findWithFilters(
                effectiveFieldIds,
                waterAllocationId,
                status,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection
        );

        List<IrrigationPlanResponse> responses = planPage.content().stream()
                .map(IrrigationPlanResponse::from)
                .toList();

        return PageResponse.<IrrigationPlanResponse>builder()
                .content(responses)
                .page(planPage.page())
                .size(planPage.size())
                .totalElements(planPage.totalElements())
                .totalPages(planPage.totalPages())
                .first(planPage.isFirst())
                .last(planPage.isLast())
                .build();
    }
}
