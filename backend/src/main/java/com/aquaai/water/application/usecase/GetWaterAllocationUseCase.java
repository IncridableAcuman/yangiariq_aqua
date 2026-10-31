package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.WaterAllocationResponse;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Retrieves details for a specific water allocation.
 * Enforces ownership access: Farmers can only access allocations for parcels they own (404 anti-enumeration).
 */
@Service
@RequiredArgsConstructor
public class GetWaterAllocationUseCase {

    private final WaterAllocationRepository waterAllocationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public WaterAllocationResponse execute(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("WaterAllocation", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        WaterAllocation allocation = waterAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WaterAllocation", id));

        // Enforce field ownership & anti-enumeration (throws 404 if farmer doesn't own parcel)
        try {
            fieldOwnershipChecker.getFieldForRead(allocation.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            // Mask existence of allocation for unowned field
            throw new ResourceNotFoundException("WaterAllocation", id);
        }

        return WaterAllocationResponse.from(allocation);
    }
}
