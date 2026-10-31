package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CancelOperationRequest;
import com.aquaai.water.api.dto.WaterAllocationResponse;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Closes an active water allocation quota.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloseWaterAllocationUseCase {

    private final WaterAllocationRepository waterAllocationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public WaterAllocationResponse execute(UUID id, CancelOperationRequest request) {
        if (id == null) {
            throw new ResourceNotFoundException("WaterAllocation", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        WaterAllocation allocation = waterAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WaterAllocation", id));

        try {
            fieldOwnershipChecker.getFieldForMutation(allocation.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("WaterAllocation", id);
        }

        allocation.close(request != null ? request.reason() : null);
        WaterAllocation saved = waterAllocationRepository.save(allocation);

        log.info("Water allocation {} closed by user {}", id, currentUserId);
        return WaterAllocationResponse.from(saved);
    }
}
