package com.aquaai.irrigation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListSmartIrrigationPlansUseCase {

    private final SmartIrrigationPlanRepository planRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    public record PagedResult<T>(
            List<T> content,
            long totalElements,
            int page,
            int size
    ) {}

    @Transactional(readOnly = true)
    public PagedResult<SmartIrrigationPlan> execute(
            UUID fieldId,
            IrrigationPlanStatus status,
            IrrigationPriority priority,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    ) {
        return execute(
                fieldId,
                status,
                priority,
                dateFrom,
                dateTo,
                page,
                size,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional(readOnly = true)
    public PagedResult<SmartIrrigationPlan> execute(
            UUID fieldId,
            IrrigationPlanStatus status,
            IrrigationPriority priority,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size,
            UUID currentUserId,
            UserRole currentRole
    ) {
        if (fieldId != null) {
            // Anti-IDOR check for requested field
            fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);
        }

        List<SmartIrrigationPlan> plans = planRepository.findFiltered(
                fieldId,
                status,
                priority,
                dateFrom,
                dateTo,
                page,
                size
        );

        long total = planRepository.countFiltered(
                fieldId,
                status,
                priority,
                dateFrom,
                dateTo
        );

        return new PagedResult<>(plans, total, page, size);
    }
}
