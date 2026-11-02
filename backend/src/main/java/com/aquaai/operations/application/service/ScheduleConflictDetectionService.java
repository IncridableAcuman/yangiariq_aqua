package com.aquaai.operations.application.service;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.operations.api.dto.ScheduleConflictDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service detecting operational scheduling conflicts:
 * - Overlapping irrigation windows on the same parcel
 * - Insufficient seasonal water quota
 * - Active hydraulic safety interlock
 */
@Service
@RequiredArgsConstructor
public class ScheduleConflictDetectionService {

    private final SmartIrrigationPlanRepository planRepository;

    public ScheduleConflictDto detectConflict(
            UUID fieldId,
            UUID currentPlanId,
            Instant requestedStart,
            Instant requestedEnd,
            BigDecimal requestedVolume,
            WaterBudget waterBudget,
            boolean hasSuspectedPipeBurst,
            boolean hasCriticalPressureDrop
    ) {
        // 1. Hydraulic Safety Interlock Precedence
        if (hasSuspectedPipeBurst || hasCriticalPressureDrop) {
            return ScheduleConflictDto.builder()
                    .hasConflict(true)
                    .conflictType("HYDRAULIC_BLOCK")
                    .conflictMessage("Hydraulic safety interlock active: Pipe burst suspected or critical pressure drop detected.")
                    .suggestedAdjustment("Resolve active hydraulic canal anomalies prior to scheduling.")
                    .build();
        }

        // 2. Water Budget / Quota Check
        if (waterBudget != null && requestedVolume != null && !waterBudget.canFulfill(requestedVolume)) {
            BigDecimal remaining = waterBudget.getRemainingAvailableM3();
            return ScheduleConflictDto.builder()
                    .hasConflict(true)
                    .conflictType("INSUFFICIENT_WATER")
                    .conflictMessage(String.format("Requested volume (%s m³) exceeds remaining seasonal allocation quota (%s m³).",
                            requestedVolume, remaining))
                    .suggestedAdjustment(String.format("Scale down irrigation volume to %s m³ or request quota increase.", remaining))
                    .build();
        }

        // 3. Time Overlap Detection on Same Field
        if (requestedStart != null && requestedEnd != null) {
            List<SmartIrrigationPlan> activePlans = planRepository.findByFieldIdIn(List.of(fieldId));
            for (SmartIrrigationPlan other : activePlans) {
                if (currentPlanId != null && currentPlanId.equals(other.getId())) {
                    continue; // Skip self
                }
                if (other.getStatus() == IrrigationPlanStatus.APPROVED
                        || other.getStatus() == IrrigationPlanStatus.SCHEDULED
                        || other.getStatus() == IrrigationPlanStatus.IN_PROGRESS) {
                    Instant otherStart = other.getPlannedStartAt();
                    Instant otherEnd = other.getPlannedEndAt();

                    if (otherStart != null && otherEnd != null) {
                        boolean overlaps = otherStart.isBefore(requestedEnd) && requestedStart.isBefore(otherEnd);
                        if (overlaps) {
                            return ScheduleConflictDto.builder()
                                    .hasConflict(true)
                                    .conflictType("TIME_OVERLAP")
                                    .conflictMessage(String.format("Schedule conflict: Field already has an irrigation turn scheduled between %s and %s.",
                                            otherStart, otherEnd))
                                    .conflictingPlanId(other.getId())
                                    .conflictingStartTime(otherStart)
                                    .conflictingEndTime(otherEnd)
                                    .suggestedAdjustment(String.format("Shift start time to after %s to prevent pump/canal overlap.", otherEnd))
                                    .build();
                        }
                    }
                }
            }
        }

        // No conflict detected
        return ScheduleConflictDto.builder()
                .hasConflict(false)
                .build();
    }
}
