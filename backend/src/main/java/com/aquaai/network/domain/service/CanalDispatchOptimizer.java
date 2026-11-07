package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.DispatchAssignment;
import com.aquaai.network.domain.model.DispatchConflict;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;
import com.aquaai.network.domain.model.FairnessPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deterministic, explainable canal dispatch optimization engine.
 * Resolves multi-parcel canal contention, enforces hydraulic safety interlocks,
 * and generates feasible, conflict-free dispatch proposals.
 */
public class CanalDispatchOptimizer {

    public record ParcelDemandRequest(
            UUID fieldId,
            String fieldName,
            UUID wuaId,
            String wuaName,
            BigDecimal requestedVolumeM3,
            BigDecimal requiredFlowRateM3S,
            int priorityRank,
            BigDecimal currentMoisturePercent,
            String cropType,
            BigDecimal remainingQuotaM3,
            UUID supplyEdgeId,
            String canalName,
            BigDecimal canalMaxCapacityM3S,
            boolean isHydraulicBlocked,
            String hydraulicBlockReason,
            Instant preferredStartAt,
            Instant preferredEndAt
    ) {}

    public record OptimizationResult(
            DispatchSchedule schedule,
            List<DispatchConflict> detectedConflicts,
            BigDecimal unmetDemandM3,
            BigDecimal efficiencyRatioPercent
    ) {}

    public OptimizationResult optimize(
            String title,
            UUID targetWuaId,
            String targetWuaName,
            List<ParcelDemandRequest> requests,
            FairnessPolicy policy,
            Instant windowStart,
            Instant windowEnd,
            UUID operatorUserId
    ) {
        if (policy == null) {
            policy = FairnessPolicy.defaultPolicy();
        }

        List<DispatchAssignment> assignments = new ArrayList<>();
        List<DispatchConflict> conflicts = new ArrayList<>();

        BigDecimal totalRequested = BigDecimal.ZERO;
        BigDecimal totalAllocated = BigDecimal.ZERO;

        if (requests == null || requests.isEmpty()) {
            DispatchSchedule emptySchedule = DispatchSchedule.builder()
                    .title(title != null ? title : "Regional Canal Dispatch Schedule")
                    .periodStart(windowStart)
                    .periodEnd(windowEnd)
                    .status(DispatchStatus.PROPOSED)
                    .targetWuaId(targetWuaId)
                    .targetWuaName(targetWuaName)
                    .totalRequestedM3(BigDecimal.ZERO)
                    .totalAllocatedM3(BigDecimal.ZERO)
                    .totalUnmetM3(BigDecimal.ZERO)
                    .assignments(Collections.emptyList())
                    .conflicts(Collections.emptyList())
                    .metrics(Map.of("message", "No active irrigation demand in selected window."))
                    .fairnessPolicy(policy)
                    .createdBy(operatorUserId)
                    .build();
            return new OptimizationResult(emptySchedule, Collections.emptyList(), BigDecimal.ZERO, new BigDecimal("100.00"));
        }

        // Group requests by supply canal segment
        Map<UUID, List<ParcelDemandRequest>> canalGroups = new HashMap<>();
        for (ParcelDemandRequest req : requests) {
            UUID canalId = req.supplyEdgeId() != null ? req.supplyEdgeId() : UUID.fromString("00000000-0000-0000-0000-000000000000");
            canalGroups.computeIfAbsent(canalId, k -> new ArrayList<>()).add(req);
        }

        for (Map.Entry<UUID, List<ParcelDemandRequest>> entry : canalGroups.entrySet()) {
            List<ParcelDemandRequest> canalRequests = entry.getValue();
            UUID canalId = entry.getKey();
            String canalName = canalRequests.get(0).canalName();
            BigDecimal canalMaxCap = canalRequests.get(0).canalMaxCapacityM3S() != null
                    ? canalRequests.get(0).canalMaxCapacityM3S() : new BigDecimal("1.50");

            // Sort competitors by priority according to policy
            sortCompetitorsByPolicy(canalRequests, policy);

            Instant rollingSlotStart = windowStart;
            BigDecimal currentSimultaneousFlow = BigDecimal.ZERO;
            List<UUID> simultaneousFieldIds = new ArrayList<>();
            List<String> simultaneousFieldNames = new ArrayList<>();

            for (ParcelDemandRequest req : canalRequests) {
                totalRequested = totalRequested.add(req.requestedVolumeM3());

                // 1. Hydraulic Safety Interlock Precedence
                if (req.isHydraulicBlocked()) {
                    conflicts.add(DispatchConflict.builder()
                            .id(UUID.randomUUID())
                            .conflictType("SAFETY_INTERLOCK_BLOCKED")
                            .severity("CRITICAL")
                            .canalSegmentId(canalId)
                            .canalName(canalName)
                            .conflictingFieldIds(List.of(req.fieldId()))
                            .conflictingFieldNames(List.of(req.fieldName()))
                            .message("Hydraulic safety interlock active on field '" + req.fieldName() + "': " + req.hydraulicBlockReason() + ". Pressurization strictly blocked.")
                            .mitigationSuggestion("Clear physical anomaly and verify pipeline integrity before dispatching water.")
                            .build());

                    assignments.add(DispatchAssignment.builder()
                            .id(UUID.randomUUID())
                            .fieldId(req.fieldId())
                            .fieldName(req.fieldName())
                            .wuaId(req.wuaId())
                            .wuaName(req.wuaName())
                            .canalSegmentId(canalId)
                            .canalName(canalName)
                            .requestedVolumeM3(req.requestedVolumeM3())
                            .allocatedVolumeM3(BigDecimal.ZERO)
                            .deliveryStartAt(windowStart)
                            .deliveryEndAt(windowStart)
                            .flowRateM3S(BigDecimal.ZERO)
                            .priorityRank(req.priorityRank())
                            .satisfactionRatePercent(BigDecimal.ZERO)
                            .rationale("BLOCKED: Hydraulic Safety Interlock active (" + req.hydraulicBlockReason() + ")")
                            .build());
                    continue;
                }

                // 2. Water Quota Guard
                BigDecimal remainingQuota = req.remainingQuotaM3() != null ? req.remainingQuotaM3() : BigDecimal.ZERO;
                BigDecimal deliverableVolume = req.requestedVolumeM3();
                if (remainingQuota.compareTo(BigDecimal.ZERO) <= 0) {
                    conflicts.add(DispatchConflict.builder()
                            .id(UUID.randomUUID())
                            .conflictType("SOURCE_DEFICIT")
                            .severity("HIGH")
                            .canalSegmentId(canalId)
                            .canalName(canalName)
                            .conflictingFieldIds(List.of(req.fieldId()))
                            .conflictingFieldNames(List.of(req.fieldName()))
                            .message("Field '" + req.fieldName() + "' has zero remaining seasonal water allocation quota.")
                            .mitigationSuggestion("Reallocate seasonal quota in Water Management before dispatching.")
                            .build());
                    deliverableVolume = BigDecimal.ZERO;
                } else if (deliverableVolume.compareTo(remainingQuota) > 0) {
                    deliverableVolume = remainingQuota;
                }

                // 3. Flow and Duration Calculation
                BigDecimal flowRate = req.requiredFlowRateM3S() != null && req.requiredFlowRateM3S().compareTo(BigDecimal.ZERO) > 0
                        ? req.requiredFlowRateM3S() : new BigDecimal("0.05");

                // Check capacity constraint on canal
                if (currentSimultaneousFlow.add(flowRate).compareTo(canalMaxCap) > 0 && !simultaneousFieldIds.isEmpty()) {
                    // Contention detected!
                    conflicts.add(DispatchConflict.builder()
                            .id(UUID.randomUUID())
                            .conflictType("CAPACITY_EXCEEDED")
                            .severity("HIGH")
                            .canalSegmentId(canalId)
                            .canalName(canalName)
                            .conflictingFieldIds(new ArrayList<>(simultaneousFieldIds))
                            .conflictingFieldNames(new ArrayList<>(simultaneousFieldNames))
                            .message("Simultaneous flow demand on canal '" + canalName + "' exceeds max carrying capacity (" + canalMaxCap + " m³/s). Staggering delivery windows.")
                            .mitigationSuggestion("Stagger irrigation run start times to avoid discharge overloading.")
                            .build());

                    // Stagger: reset simultaneous window
                    currentSimultaneousFlow = BigDecimal.ZERO;
                    simultaneousFieldIds.clear();
                    simultaneousFieldNames.clear();
                }

                // Calculate duration in hours based on flow rate: Volume / (FlowRate * 3600)
                long durationSeconds = 3600 * 4; // default 4 hours
                if (flowRate.compareTo(BigDecimal.ZERO) > 0 && deliverableVolume.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal seconds = deliverableVolume.divide(flowRate, 0, RoundingMode.HALF_UP);
                    durationSeconds = Math.max(3600, Math.min(seconds.longValue(), 3600 * 12));
                }

                Instant runStart = rollingSlotStart;
                Instant runEnd = runStart.plusSeconds(durationSeconds);
                if (runEnd.isAfter(windowEnd)) {
                    runEnd = windowEnd;
                }

                BigDecimal satisfactionRate = BigDecimal.ZERO;
                if (req.requestedVolumeM3().compareTo(BigDecimal.ZERO) > 0) {
                    satisfactionRate = deliverableVolume.multiply(new BigDecimal("100.00"))
                            .divide(req.requestedVolumeM3(), 2, RoundingMode.HALF_UP);
                }

                totalAllocated = totalAllocated.add(deliverableVolume);

                assignments.add(DispatchAssignment.builder()
                        .id(UUID.randomUUID())
                        .fieldId(req.fieldId())
                        .fieldName(req.fieldName())
                        .wuaId(req.wuaId())
                        .wuaName(req.wuaName())
                        .canalSegmentId(canalId)
                        .canalName(canalName)
                        .requestedVolumeM3(req.requestedVolumeM3())
                        .allocatedVolumeM3(deliverableVolume)
                        .deliveryStartAt(runStart)
                        .deliveryEndAt(runEnd)
                        .flowRateM3S(flowRate)
                        .priorityRank(req.priorityRank())
                        .satisfactionRatePercent(satisfactionRate)
                        .rationale("Scheduled delivery via " + canalName + " (" + flowRate + " m³/s). Policy: " + policy.policyType())
                        .build());

                currentSimultaneousFlow = currentSimultaneousFlow.add(flowRate);
                simultaneousFieldIds.add(req.fieldId());
                simultaneousFieldNames.add(req.fieldName());

                // Stagger rolling slot forward by 2 hours for next parcel to spread peak load
                rollingSlotStart = rollingSlotStart.plus(Duration.ofHours(2));
                if (rollingSlotStart.isAfter(windowEnd.minus(Duration.ofHours(2)))) {
                    rollingSlotStart = windowStart;
                }
            }
        }

        BigDecimal totalUnmet = totalRequested.subtract(totalAllocated);
        if (totalUnmet.compareTo(BigDecimal.ZERO) < 0) totalUnmet = BigDecimal.ZERO;

        BigDecimal efficiencyRatio = BigDecimal.ZERO;
        if (totalRequested.compareTo(BigDecimal.ZERO) > 0) {
            efficiencyRatio = totalAllocated.multiply(new BigDecimal("100.00"))
                    .divide(totalRequested, 2, RoundingMode.HALF_UP);
        }

        DispatchSchedule schedule = DispatchSchedule.builder()
                .title(title != null ? title : "Coordinated Canal Dispatch Proposal")
                .periodStart(windowStart)
                .periodEnd(windowEnd)
                .status(conflicts.isEmpty() ? DispatchStatus.PROPOSED : DispatchStatus.REVIEW_REQUIRED)
                .targetWuaId(targetWuaId)
                .targetWuaName(targetWuaName)
                .totalRequestedM3(totalRequested)
                .totalAllocatedM3(totalAllocated)
                .totalUnmetM3(totalUnmet)
                .assignments(assignments)
                .conflicts(conflicts)
                .metrics(Map.of(
                        "efficiencyRatioPercent", efficiencyRatio,
                        "conflictCount", conflicts.size(),
                        "assignmentCount", assignments.size(),
                        "fairnessPolicyApplied", policy.policyType()
                ))
                .fairnessPolicy(policy)
                .createdBy(operatorUserId)
                .build();

        return new OptimizationResult(schedule, conflicts, totalUnmet, efficiencyRatio);
    }

    private void sortCompetitorsByPolicy(List<ParcelDemandRequest> list, FairnessPolicy policy) {
        if ("MOISTURE_DEFICIT_PRIORITY".equalsIgnoreCase(policy.policyType())) {
            // Lowest moisture first
            list.sort(Comparator.comparing(ParcelDemandRequest::currentMoisturePercent, Comparator.nullsLast(BigDecimal::compareTo)));
        } else if ("PROPORTIONAL_DEFICIT".equalsIgnoreCase(policy.policyType())) {
            // Proportional: sort by requested volume ascending
            list.sort(Comparator.comparing(ParcelDemandRequest::requestedVolumeM3));
        } else {
            // CROP_CRITICALITY_FIRST (Default): priority rank ascending (1 = highest urgency)
            list.sort(Comparator.comparingInt(ParcelDemandRequest::priorityRank));
        }
    }
}
