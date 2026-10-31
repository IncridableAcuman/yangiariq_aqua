package com.aquaai.water.application.usecase;

import com.aquaai.shared.api.PageResponse;
import com.aquaai.water.api.dto.WaterSourceResponse;
import com.aquaai.water.domain.model.WaterSourcePage;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case: Lists regional water sources with optional filtering and pagination.
 * Open to all authenticated users.
 */
@Service
@RequiredArgsConstructor
public class ListWaterSourcesUseCase {

    private final WaterSourceRepository waterSourceRepository;

    @Transactional(readOnly = true)
    public PageResponse<WaterSourceResponse> execute(
            String search,
            WaterSourceType type,
            WaterSourceStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        WaterSourcePage sourcePage = waterSourceRepository.findWithFilters(
                search,
                type,
                status,
                page,
                size,
                sortBy,
                sortDirection
        );

        List<WaterSourceResponse> responses = sourcePage.content().stream()
                .map(WaterSourceResponse::from)
                .toList();

        return PageResponse.<WaterSourceResponse>builder()
                .content(responses)
                .page(sourcePage.page())
                .size(sourcePage.size())
                .totalElements(sourcePage.totalElements())
                .totalPages(sourcePage.totalPages())
                .first(sourcePage.isFirst())
                .last(sourcePage.isLast())
                .build();
    }
}
