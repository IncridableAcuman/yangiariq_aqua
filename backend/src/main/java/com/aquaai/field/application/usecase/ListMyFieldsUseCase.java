package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldPage;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case: Lists field parcels with filtering and pagination.
 * For FARMER users, restricts results strictly to parcels owned by the authenticated farmer.
 * For OPERATOR and ADMIN users, allows querying across all parcels or filtering by specific owner.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListMyFieldsUseCase {

    private final FieldRepository fieldRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public PageResponse<FieldResponse> execute(
            FieldStatus status,
            CropType cropType,
            SoilType soilType,
            IrrigationMethod irrigationMethod,
            UUID filterOwnerId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        UUID effectiveOwnerId;
        if (currentRole == UserRole.FARMER) {
            // Farmers only see their own fields; ignore any passed filterOwnerId parameter
            effectiveOwnerId = currentUserId;
        } else {
            // Admin and Operator can query by specific owner or list district-wide
            effectiveOwnerId = filterOwnerId;
        }

        FieldPage fieldPage = fieldRepository.findWithFilters(
                effectiveOwnerId,
                status,
                cropType,
                soilType,
                irrigationMethod,
                page,
                size,
                sortBy,
                direction
        );

        List<FieldResponse> responses = fieldPage.content().stream()
                .map(FieldResponse::from)
                .toList();

        return PageResponse.<FieldResponse>builder()
                .content(responses)
                .page(fieldPage.page())
                .size(fieldPage.size())
                .totalElements(fieldPage.totalElements())
                .totalPages(fieldPage.totalPages())
                .first(fieldPage.isFirst())
                .last(fieldPage.isLast())
                .build();
    }
}
