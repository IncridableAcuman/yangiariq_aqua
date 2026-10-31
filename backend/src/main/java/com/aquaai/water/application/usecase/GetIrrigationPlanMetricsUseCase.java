package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.IrrigationPlanMetricsResponse;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.model.PlannedVsActualMetrics;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import com.aquaai.water.domain.service.WaterConsumptionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case: Derives planned vs actual water delivery variance and operational consumption ratio
 * for a scheduled irrigation plan.
 * <p>
 * Enforces ownership access: Farmers can only view metrics for plans on parcels they own (404 anti-enumeration).
 */
@Service
@RequiredArgsConstructor
public class GetIrrigationPlanMetricsUseCase {

    private final IrrigationPlanRepository irrigationPlanRepository;
    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public IrrigationPlanMetricsResponse execute(UUID planId) {
        if (planId == null) {
            throw new ResourceNotFoundException("IrrigationPlan", "null");
        }

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        IrrigationPlan plan = irrigationPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("IrrigationPlan", planId));

        // Enforce field ownership & anti-enumeration (throws 404 if farmer doesn't own parcel)
        try {
            fieldOwnershipChecker.getFieldForRead(plan.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("IrrigationPlan", planId);
        }

        BigDecimal actualVolume = waterConsumptionQueryRepository.getConsumedVolumeByPlanId(planId);
        long completedEventsCount = waterConsumptionQueryRepository.countCompletedEventsByPlanId(planId);

        PlannedVsActualMetrics metrics = WaterConsumptionCalculator.calculatePlannedVsActual(
                plan.getPlannedVolumeM3(),
                actualVolume
        );

        return new IrrigationPlanMetricsResponse(
                plan.getId(),
                plan.getFieldId(),
                plan.getWaterAllocationId(),
                metrics.plannedVolumeM3(),
                metrics.actualVolumeM3(),
                metrics.varianceVolumeM3(),
                metrics.absoluteVarianceVolumeM3(),
                metrics.variancePercentage(),
                metrics.consumptionRatioPercentage(),
                completedEventsCount,
                plan.getStatus()
        );
    }
}
