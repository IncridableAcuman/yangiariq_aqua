package com.aquaai.network.infrastructure.adapter;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.network.domain.port.NetworkIrrigationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NetworkIrrigationAdapter implements NetworkIrrigationPort {

    private final SmartIrrigationPlanRepository planRepository;

    @Override
    public List<SmartIrrigationPlan> getUpcomingPlansForField(UUID fieldId) {
        if (fieldId == null) return Collections.emptyList();
        List<SmartIrrigationPlan> all = planRepository.findByFieldId(fieldId);
        if (all == null) return Collections.emptyList();
        return all.stream()
                .filter(p -> p.getStatus() == IrrigationPlanStatus.PROPOSED
                        || p.getStatus() == IrrigationPlanStatus.APPROVED
                        || p.getStatus() == IrrigationPlanStatus.SCHEDULED)
                .collect(Collectors.toList());
    }

    @Override
    public List<SmartIrrigationPlan> getAllActivePlans() {
        return planRepository.findFiltered(null, null, null, null, null, 0, 1000).stream()
                .filter(p -> p.getStatus() != null && p.getStatus().isActive())
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasScheduleConflict(UUID fieldId) {
        if (fieldId == null) return false;
        List<SmartIrrigationPlan> activePlans = planRepository.findByFieldId(fieldId).stream()
                .filter(p -> p.getStatus() != null && p.getStatus().isActive())
                .toList();

        for (int i = 0; i < activePlans.size(); i++) {
            for (int j = i + 1; j < activePlans.size(); j++) {
                SmartIrrigationPlan p1 = activePlans.get(i);
                SmartIrrigationPlan p2 = activePlans.get(j);
                if (p1.getPlannedStartAt() != null && p1.getPlannedEndAt() != null
                        && p2.getPlannedStartAt() != null && p2.getPlannedEndAt() != null) {
                    if (p1.getPlannedStartAt().isBefore(p2.getPlannedEndAt())
                            && p2.getPlannedStartAt().isBefore(p1.getPlannedEndAt())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
