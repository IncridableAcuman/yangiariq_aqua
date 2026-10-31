package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationPlanResponse;
import com.aquaai.water.api.dto.UpdateIrrigationPlanRequest;
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
 * Use case: Updates details for an irrigation plan in PLANNED status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateIrrigationPlanUseCase {

    private final IrrigationPlanRepository irrigationPlanRepository;
    private final WaterAllocationRepository waterAllocationRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IrrigationPlanResponse execute(UUID id, UpdateIrrigationPlanRequest request) {
        if (id == null) {
            throw new ResourceNotFoundException("IrrigationPlan", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        IrrigationPlan plan = irrigationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IrrigationPlan", id));

        try {
            fieldOwnershipChecker.getFieldForMutation(plan.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("IrrigationPlan", id);
        }

        if (request.waterAllocationId() != null) {
            WaterAllocation allocation = waterAllocationRepository.findById(request.waterAllocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("WaterAllocation", request.waterAllocationId()));

            if (!allocation.getFieldId().equals(plan.getFieldId())) {
                throw new BusinessRuleException("Water allocation " + request.waterAllocationId() + " does not belong to field " + plan.getFieldId());
            }

            if (!allocation.isActive()) {
                throw new BusinessRuleException("Cannot link irrigation plan to water allocation in status " + allocation.getStatus());
            }
        }

        plan.updateDetails(
                request.waterAllocationId(),
                request.plannedStartAt(),
                request.plannedEndAt(),
                request.plannedVolumeM3(),
                request.method(),
                request.notes()
        );

        IrrigationPlan saved = irrigationPlanRepository.save(plan);
        log.info("Irrigation plan {} updated by user {}", id, currentUserId);
        return IrrigationPlanResponse.from(saved);
    }
}
