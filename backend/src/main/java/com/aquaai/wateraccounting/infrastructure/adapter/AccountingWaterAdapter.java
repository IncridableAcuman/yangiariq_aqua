package com.aquaai.wateraccounting.infrastructure.adapter;

import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import com.aquaai.wateraccounting.domain.port.AccountingWaterPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountingWaterAdapter implements AccountingWaterPort {

    private final WaterAllocationRepository allocationRepository;
    private final IrrigationEventRepository eventRepository;

    @Override
    public List<AllocationSummary> getAllAllocations() {
        return allocationRepository.findAll().stream()
                .map(this::mapAllocation)
                .collect(Collectors.toList());
    }

    @Override
    public List<AllocationSummary> getAllocationsForField(UUID fieldId) {
        return allocationRepository.findByFieldId(fieldId).stream()
                .map(this::mapAllocation)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionSummary> getCompletedConsumptions() {
        var page = eventRepository.findWithFilters(null, null, IrrigationEventStatus.COMPLETED, null, null, 0, 5000, "startedAt", "DESC");
        if (page == null || page.content() == null) {
            return new ArrayList<>();
        }
        return page.content().stream()
                .map(this::mapConsumption)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionSummary> getConsumptionsForField(UUID fieldId) {
        return eventRepository.findByFieldIdAndStatus(fieldId, IrrigationEventStatus.COMPLETED).stream()
                .map(this::mapConsumption)
                .collect(Collectors.toList());
    }

    private AllocationSummary mapAllocation(WaterAllocation a) {
        return new AllocationSummary(
                a.getId(),
                a.getFieldId(),
                a.getWaterSourceId(),
                a.getAllocatedVolumeM3(),
                a.getPeriodStart(),
                a.getPeriodEnd(),
                a.getStatus() != null ? a.getStatus().name() : "ACTIVE"
        );
    }

    private ConsumptionSummary mapConsumption(IrrigationEvent e) {
        return new ConsumptionSummary(
                e.getId(),
                e.getFieldId(),
                e.getActualVolumeM3(),
                e.getStartedAt(),
                e.getEndedAt()
        );
    }
}
