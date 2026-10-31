package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CreateIrrigationPlanRequest;
import com.aquaai.water.api.dto.IrrigationPlanResponse;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Creates a scheduled irrigation plan for an agricultural parcel.
 * Enforces field ownership, cross-aggregate allocation alignment, and active quota status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateIrrigationPlanUseCase {

    private final IrrigationPlanRepository irrigationPlanRepository;
    private final WaterAllocationRepository waterAllocationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IrrigationPlanResponse execute(CreateIrrigationPlanRequest request) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field ownership & anti-enumeration
        fieldOwnershipChecker.getFieldForMutation(request.fieldId(), currentUserId, currentRole);

        // Cross-aggregate validation if waterAllocationId is specified
        if (request.waterAllocationId() != null) {
            WaterAllocation allocation = waterAllocationRepository.findById(request.waterAllocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("WaterAllocation", request.waterAllocationId()));

            // Consistency invariant: allocation must belong to the exact same parcel
            if (!allocation.getFieldId().equals(request.fieldId())) {
                throw new BusinessRuleException("Water allocation " + request.waterAllocationId() + " does not belong to field " + request.fieldId());
            }

            // Quota must be ACTIVE to link new plans
            if (!allocation.isActive()) {
                throw new BusinessRuleException("Cannot link irrigation plan to water allocation in status " + allocation.getStatus());
            }
        }

        IrrigationPlan plan = IrrigationPlan.create(
                request.fieldId(),
                request.waterAllocationId(),
                request.plannedStartAt(),
                request.plannedEndAt(),
                request.plannedVolumeM3(),
                request.method(),
                request.notes()
        );

        IrrigationPlan saved = irrigationPlanRepository.save(plan);
        log.info("Irrigation plan {} created for field {} by user {}", saved.getId(), saved.getFieldId(), currentUserId);
        return IrrigationPlanResponse.from(saved);
    }
}
