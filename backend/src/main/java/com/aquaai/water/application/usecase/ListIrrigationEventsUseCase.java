package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationEventResponse;
import com.aquaai.water.domain.model.IrrigationEventPage;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Use case: Lists executed irrigation events with filtering and pagination.
 * Restricts FARMER queries strictly to parcels owned by the authenticated caller.
 */
@Service
@RequiredArgsConstructor
public class ListIrrigationEventsUseCase {

    private final IrrigationEventRepository irrigationEventRepository;
    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public PageResponse<IrrigationEventResponse> execute(
            UUID fieldId,
            UUID irrigationPlanId,
            IrrigationEventStatus status,
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
                    return PageResponse.<IrrigationEventResponse>builder()
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

        IrrigationEventPage eventPage = irrigationEventRepository.findWithFilters(
                effectiveFieldIds,
                irrigationPlanId,
                status,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection
        );

        List<IrrigationEventResponse> responses = eventPage.content().stream()
                .map(IrrigationEventResponse::from)
                .toList();

        return PageResponse.<IrrigationEventResponse>builder()
                .content(responses)
                .page(eventPage.page())
                .size(eventPage.size())
                .totalElements(eventPage.totalElements())
                .totalPages(eventPage.totalPages())
                .first(eventPage.isFirst())
                .last(eventPage.isLast())
                .build();
    }
}
