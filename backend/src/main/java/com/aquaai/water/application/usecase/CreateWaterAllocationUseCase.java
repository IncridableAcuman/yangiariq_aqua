package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CreateWaterAllocationRequest;
import com.aquaai.water.api.dto.WaterAllocationResponse;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Establishes a volumetric water quota allocation for an agricultural parcel.
 * Enforces field ownership, anti-enumeration, and active water source validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateWaterAllocationUseCase {

    private final WaterAllocationRepository waterAllocationRepository;
    private final WaterSourceRepository waterSourceRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public WaterAllocationResponse execute(CreateWaterAllocationRequest request) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field ownership & anti-enumeration
        fieldOwnershipChecker.getFieldForMutation(request.fieldId(), currentUserId, currentRole);

        // Verify water source exists and is ACTIVE
        WaterSource source = waterSourceRepository.findById(request.waterSourceId())
                .orElseThrow(() -> new ResourceNotFoundException("WaterSource", request.waterSourceId()));

        if (!source.isActive()) {
            throw new BusinessRuleException("Cannot allocate water from an INACTIVE water source: " + source.getName());
        }

        WaterAllocation allocation = WaterAllocation.create(
                request.fieldId(),
                request.waterSourceId(),
                request.allocatedVolumeM3(),
                request.periodStart(),
                request.periodEnd(),
                request.notes()
        );

        WaterAllocation saved = waterAllocationRepository.save(allocation);
        log.info("Water allocation {} created for field {} from source {} by user {}",
                saved.getId(), saved.getFieldId(), saved.getWaterSourceId(), currentUserId);
        return WaterAllocationResponse.from(saved);
    }
}
