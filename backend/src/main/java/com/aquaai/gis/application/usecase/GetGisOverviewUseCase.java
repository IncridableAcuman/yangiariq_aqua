package com.aquaai.gis.application.usecase;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.gis.api.dto.GisAllocationLinkDto;
import com.aquaai.gis.api.dto.GisFieldDto;
import com.aquaai.gis.api.dto.GisFieldWaterStatusDto;
import com.aquaai.gis.api.dto.GisOverviewResponse;
import com.aquaai.gis.api.dto.GisWaterSourceDto;
import com.aquaai.gis.domain.model.WaterStatusCategory;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.DateRangeDto;
import com.aquaai.water.domain.model.FieldWaterConsumption;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import com.aquaai.water.domain.service.WaterConsumptionCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates spatial GIS intelligence aggregating accessible parcel polygons,
 * regional water source infrastructure, volumetric quota statuses, and allocation links.
 * <p>
 * Zero N+1 Queries: Uses optimized batch repository queries.
 * Tenant Isolation & Anti-IDOR: Farmers receive only parcels they own and their own quota links.
 * District Monitoring: Operators and Admins monitor all district assets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetGisOverviewUseCase {

    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("80.00");
    private static final BigDecimal OVER_CONSUMED_THRESHOLD = new BigDecimal("100.00");

    private final FieldRepository fieldRepository;
    private final WaterSourceRepository waterSourceRepository;
    private final WaterAllocationRepository waterAllocationRepository;
    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public GisOverviewResponse execute(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessRuleException(
                    "Invalid period: 'from' timestamp must not be after 'to' timestamp",
                    "INVALID_DATE_RANGE"
            );
        }

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        List<Field> accessibleFields;
        List<WaterAllocation> accessibleAllocations;

        if (currentRole == UserRole.FARMER) {
            // Strict Tenant Isolation: Farmers only see own fields
            accessibleFields = fieldRepository.findByOwnerId(currentUserId);
            List<UUID> fieldIds = accessibleFields.stream().map(Field::getId).toList();
            accessibleAllocations = fieldIds.isEmpty()
                    ? List.of()
                    : waterAllocationRepository.findByFieldIdIn(fieldIds);
        } else {
            // Operators and Admins have district-wide visibility
            accessibleFields = fieldRepository.findAll();
            accessibleAllocations = waterAllocationRepository.findAll();
        }

        List<UUID> accessibleFieldIds = accessibleFields.stream().map(Field::getId).toList();
        List<WaterSource> waterSources = waterSourceRepository.findAll();

        // Batch-aggregate water balances across all accessible fields (2 SQL queries total, 0 N+1)
        Map<UUID, FieldWaterSummaryData> summaryDataMap = accessibleFieldIds.isEmpty()
                ? Map.of()
                : waterConsumptionQueryRepository.getFieldWaterSummaries(accessibleFieldIds, from, to);

        List<GisFieldWaterStatusDto> waterStatuses = new ArrayList<>();
        for (Field field : accessibleFields) {
            FieldWaterSummaryData data = summaryDataMap.getOrDefault(
                    field.getId(),
                    new FieldWaterSummaryData(BigDecimal.ZERO, 0, BigDecimal.ZERO, 0)
            );
            FieldWaterConsumption consumption = WaterConsumptionCalculator.calculateFieldConsumption(
                    field.getId(),
                    from,
                    to,
                    data
            );

            WaterStatusCategory category = determineCategory(consumption);

            waterStatuses.add(new GisFieldWaterStatusDto(
                    consumption.fieldId(),
                    consumption.totalAllocatedVolumeM3(),
                    consumption.totalConsumedVolumeM3(),
                    consumption.totalRemainingVolumeM3(),
                    consumption.totalOverConsumedVolumeM3(),
                    consumption.isOverConsumed(),
                    consumption.consumptionPercentage(),
                    category,
                    consumption.allocationCount(),
                    consumption.irrigationEventCount()
            ));
        }

        List<GisFieldDto> fieldDtos = accessibleFields.stream()
                .map(GisFieldDto::from)
                .toList();

        List<GisWaterSourceDto> waterSourceDtos = waterSources.stream()
                .map(GisWaterSourceDto::from)
                .toList();

        List<GisAllocationLinkDto> allocationDtos = accessibleAllocations.stream()
                .map(GisAllocationLinkDto::from)
                .toList();

        DateRangeDto periodDto = (from != null || to != null) ? new DateRangeDto(from, to) : null;

        log.debug("GIS overview generated for user {} ({}) with {} fields, {} sources, {} allocations",
                currentUserId, currentRole, fieldDtos.size(), waterSourceDtos.size(), allocationDtos.size());

        return new GisOverviewResponse(
                fieldDtos,
                waterSourceDtos,
                waterStatuses,
                allocationDtos,
                periodDto
        );
    }

    private WaterStatusCategory determineCategory(FieldWaterConsumption consumption) {
        if (consumption.totalAllocatedVolumeM3().compareTo(BigDecimal.ZERO) == 0
                && consumption.totalConsumedVolumeM3().compareTo(BigDecimal.ZERO) == 0) {
            return WaterStatusCategory.NO_ALLOCATION;
        }

        if (consumption.isOverConsumed()
                || consumption.consumptionPercentage().compareTo(OVER_CONSUMED_THRESHOLD) > 0) {
            return WaterStatusCategory.OVER_CONSUMED;
        }

        if (consumption.consumptionPercentage().compareTo(WARNING_THRESHOLD) >= 0) {
            return WaterStatusCategory.WARNING;
        }

        return WaterStatusCategory.NORMAL;
    }
}
