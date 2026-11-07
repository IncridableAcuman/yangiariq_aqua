package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.wateraccounting.api.dto.ReconciliationReviewRequest;
import com.aquaai.wateraccounting.api.dto.WaterReconciliationResponse;
import com.aquaai.wateraccounting.application.service.WaterAccountingDtoMapper;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReconciliationUseCase {

    private final WaterReconciliationRepository reconciliationRepository;
    private final WaterAccountingDtoMapper dtoMapper;

    @Transactional
    public WaterReconciliationResponse execute(UUID id, ReconciliationReviewRequest request, UUID currentUserId) {
        WaterReconciliationItem item = reconciliationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water reconciliation record", id));

        item.review(currentUserId, request != null ? request.notes() : "Reviewed by operator");
        var saved = reconciliationRepository.save(item);

        log.info("Recorded review for water reconciliation: id={}, reviewerId={}, newStatus={}",
                saved.getId(), currentUserId, saved.getStatus());

        return dtoMapper.toReconciliationResponse(saved);
    }
}
