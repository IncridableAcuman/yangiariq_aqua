package com.aquaai.risk.infrastructure.adapter;

import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.operations.api.dto.ScheduleConflictDto;
import com.aquaai.operations.application.service.ScheduleConflictDetectionService;
import com.aquaai.risk.domain.port.RiskIrrigationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskIrrigationAdapter implements RiskIrrigationPort {

    private final SmartIrrigationPlanRepository planRepository;
    private final ScheduleConflictDetectionService conflictDetectionService;

    @Override
    public List<SmartIrrigationPlan> getActivePlans(UUID fieldId) {
        if (fieldId == null) return Collections.emptyList();
        List<SmartIrrigationPlan> all = planRepository.findByFieldId(fieldId);
        if (all == null) return Collections.emptyList();
        return all.stream()
                .filter(p -> p.getStatus().isActive())
                .toList();
    }

    @Override
    public boolean hasScheduleConflict(UUID fieldId) {
        if (fieldId == null) return false;
        List<SmartIrrigationPlan> activePlans = getActivePlans(fieldId);
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
