package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.wateraccounting.api.dto.WaterReconciliationResponse;
import com.aquaai.wateraccounting.application.service.WaterAccountingDtoMapper;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetWaterReconciliationUseCase {

    private final WaterReconciliationRepository reconciliationRepository;
    private final WaterAccountingDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<WaterReconciliationResponse> execute(String boundaryTypeFilter) {
        List<WaterReconciliationItem> items;
        if (boundaryTypeFilter != null && !boundaryTypeFilter.isBlank()) {
            items = reconciliationRepository.findByBoundaryType(boundaryTypeFilter.toUpperCase());
        } else {
            items = reconciliationRepository.findAll();
        }

        return items.stream()
                .map(dtoMapper::toReconciliationResponse)
                .collect(Collectors.toList());
    }
}
