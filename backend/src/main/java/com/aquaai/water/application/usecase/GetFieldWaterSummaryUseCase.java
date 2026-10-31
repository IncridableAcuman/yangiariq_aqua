package com.aquaai.water.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.DateRangeDto;
import com.aquaai.water.api.dto.FieldWaterSummaryResponse;
import com.aquaai.water.domain.model.FieldWaterConsumption;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import com.aquaai.water.domain.service.WaterConsumptionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case: Aggregates periodic water consumption, remaining balance, and efficiency metrics
 * for an agricultural parcel.
 * <p>
 * Enforces ownership access: Farmers can only view summaries for parcels they own (404 anti-enumeration).
 * Operators and Administrators have district-wide monitoring access.
 */
@Service
@RequiredArgsConstructor
public class GetFieldWaterSummaryUseCase {

    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public FieldWaterSummaryResponse execute(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessRuleException(
                    "Invalid period: 'from' timestamp must not be after 'to' timestamp",
                    "INVALID_DATE_RANGE"
            );
        }

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce field ownership & anti-enumeration (throws 404 for non-existent or unowned fields)
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        FieldWaterSummaryData rawData = waterConsumptionQueryRepository.getFieldWaterSummary(fieldId, from, to);
        FieldWaterConsumption consumption = WaterConsumptionCalculator.calculateFieldConsumption(fieldId, from, to, rawData);

        DateRangeDto periodDto = (from != null || to != null) ? new DateRangeDto(from, to) : null;

        return new FieldWaterSummaryResponse(
                consumption.fieldId(),
                periodDto,
                consumption.totalAllocatedVolumeM3(),
                consumption.totalConsumedVolumeM3(),
                consumption.totalRemainingVolumeM3(),
                consumption.totalOverConsumedVolumeM3(),
                consumption.isOverConsumed(),
                consumption.consumptionPercentage(),
                consumption.allocationCount(),
                consumption.irrigationEventCount()
        );
    }
}
