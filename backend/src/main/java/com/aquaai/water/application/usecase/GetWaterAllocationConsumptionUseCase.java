package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.WaterAllocationConsumptionResponse;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterConsumptionMetrics;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import com.aquaai.water.domain.service.WaterConsumptionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case: Derives real-time volumetric consumption, remaining balance, utilization ratio,
 * and over-consumption indicators for an agricultural parcel water allocation.
 * <p>
 * Enforces ownership access: Farmers can only view allocations for parcels they own (404 anti-enumeration).
 */
@Service
@RequiredArgsConstructor
public class GetWaterAllocationConsumptionUseCase {

    private final WaterAllocationRepository waterAllocationRepository;
    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public WaterAllocationConsumptionResponse execute(UUID allocationId) {
        if (allocationId == null) {
            throw new ResourceNotFoundException("WaterAllocation", "null");
        }

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        WaterAllocation allocation = waterAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("WaterAllocation", allocationId));

        // Enforce field ownership & anti-enumeration (throws 404 if farmer doesn't own parcel)
        try {
            fieldOwnershipChecker.getFieldForRead(allocation.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("WaterAllocation", allocationId);
        }

        BigDecimal consumedVolume = waterConsumptionQueryRepository.getConsumedVolumeByAllocationId(allocationId);
        long completedEventsCount = waterConsumptionQueryRepository.countCompletedEventsByAllocationId(allocationId);

        WaterConsumptionMetrics metrics = WaterConsumptionCalculator.calculateAllocationConsumption(
                allocation.getAllocatedVolumeM3(),
                consumedVolume
        );

        return new WaterAllocationConsumptionResponse(
                allocation.getId(),
                allocation.getFieldId(),
                allocation.getWaterSourceId(),
                metrics.allocatedVolumeM3(),
                metrics.consumedVolumeM3(),
                metrics.remainingVolumeM3(),
                metrics.consumptionPercentage(),
                metrics.isOverConsumed(),
                metrics.overConsumedVolumeM3(),
                completedEventsCount,
                allocation.getStatus(),
                allocation.getPeriodStart(),
                allocation.getPeriodEnd()
        );
    }
}
