package com.aquaai.irrigation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFieldWaterBudgetUseCase {

    private final WaterBudgetDataPort waterBudgetDataPort;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public WaterBudget execute(UUID fieldId) {
        return execute(
                fieldId,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional(readOnly = true)
    public WaterBudget execute(UUID fieldId, UUID currentUserId, UserRole currentRole) {
        if (fieldId == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        // Anti-IDOR check (throws 404 if unauthorized farmer)
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        return waterBudgetDataPort.getWaterBudgetForField(fieldId)
                .orElse(WaterBudget.calculate(fieldId, null, null, null, null));
    }
}
