package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationPlanResponse;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Retrieves details for a specific scheduled irrigation plan.
 * Enforces ownership: Farmers access only plans for parcels they own (404 anti-enumeration).
 */
@Service
@RequiredArgsConstructor
public class GetIrrigationPlanUseCase {

    private final IrrigationPlanRepository irrigationPlanRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public IrrigationPlanResponse execute(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("IrrigationPlan", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        IrrigationPlan plan = irrigationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IrrigationPlan", id));

        try {
            fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("IrrigationPlan", id);
        }

        return IrrigationPlanResponse.from(plan);
    }
}
