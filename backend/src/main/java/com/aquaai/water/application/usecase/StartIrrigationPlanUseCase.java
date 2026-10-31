package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationPlanResponse;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Transitions an irrigation plan from PLANNED to IN_PROGRESS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartIrrigationPlanUseCase {

    private final IrrigationPlanRepository irrigationPlanRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IrrigationPlanResponse execute(UUID id) {
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

        plan.start();
        IrrigationPlan saved = irrigationPlanRepository.save(plan);

        log.info("Irrigation plan {} started by user {}", id, currentUserId);
        return IrrigationPlanResponse.from(saved);
    }
}
