package com.aquaai.network.application.usecase;

import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.network.api.dto.DispatchOptimizationRequest;
import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.FairnessPolicy;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.port.NetworkAnomalyPort;
import com.aquaai.network.domain.port.NetworkFieldPort;
import com.aquaai.network.domain.port.NetworkIrrigationPort;
import com.aquaai.network.domain.port.NetworkNotificationPort;
import com.aquaai.network.domain.port.NetworkWaterPort;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import com.aquaai.network.domain.service.CanalDispatchOptimizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizeCanalDispatchUseCase {

    private final NetworkFieldPort fieldPort;
    private final NetworkWaterPort waterPort;
    private final NetworkIrrigationPort irrigationPort;
    private final NetworkAnomalyPort anomalyPort;
    private final NetworkNotificationPort notificationPort;
    private final WaterNetworkNodeRepository nodeRepository;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterUserAssociationRepository wuaRepository;
    private final DispatchScheduleRepository dispatchScheduleRepository;
    private final CanalDispatchOptimizer dispatchOptimizer;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional
    public DispatchScheduleResponse execute(DispatchOptimizationRequest request, UUID currentUserId) {
        Instant periodStart = request.periodStart() != null ? request.periodStart() : Instant.now();
        Instant periodEnd = request.periodEnd() != null ? request.periodEnd() : periodStart.plusSeconds(86400 * 7);

        UUID targetWuaId = request.targetWuaId();
        String targetWuaName = "District Wide";
        if (targetWuaId != null) {
            targetWuaName = wuaRepository.findById(targetWuaId)
                    .map(WaterUserAssociation::getName)
                    .orElse("WUA " + targetWuaId);
        }

        String policyType = "CROP_CRITICALITY_FIRST";
        if (request.fairnessPolicyType() != null && !request.fairnessPolicyType().isBlank()) {
            policyType = request.fairnessPolicyType().toUpperCase();
        }
        FairnessPolicy fairnessPolicy = new FairnessPolicy(
                policyType,
                "Applied fairness optimization rule: " + policyType,
                new BigDecimal("40.00")
        );

        List<NetworkFieldPort.FieldParcelInfo> fields = fieldPort.getAllFields();
        List<WaterNetworkNode> nodes = nodeRepository.findAll();
        List<WaterNetworkEdge> edges = edgeRepository.findAll();

        Map<UUID, UUID> fieldToEdgeId = new HashMap<>();
        Map<UUID, String> edgeNameMap = new HashMap<>();
        Map<UUID, BigDecimal> edgeCapacityMap = new HashMap<>();
        for (WaterNetworkEdge edge : edges) {
            edgeNameMap.put(edge.getId(), edge.getName());
            edgeCapacityMap.put(edge.getId(), edge.getMaxCapacityM3S());
        }

        Map<UUID, UUID> fieldToWuaId = new HashMap<>();
        for (WaterNetworkNode node : nodes) {
            if (node.getFieldId() != null) {
                if (node.getWuaId() != null) {
                    fieldToWuaId.put(node.getFieldId(), node.getWuaId());
                }
                for (WaterNetworkEdge edge : edges) {
                    if (edge.getEndNodeId().equals(node.getId())) {
                        fieldToEdgeId.put(node.getFieldId(), edge.getId());
                        break;
                    }
                }
            }
        }

        List<CanalDispatchOptimizer.ParcelDemandRequest> demandRequests = new ArrayList<>();

        for (NetworkFieldPort.FieldParcelInfo f : fields) {
            UUID wuaId = fieldToWuaId.get(f.id());
            if (targetWuaId != null && !targetWuaId.equals(wuaId)) {
                continue; // filter by target WUA if specified
            }

            UUID edgeId = fieldToEdgeId.get(f.id());
            String canalName = edgeId != null ? edgeNameMap.getOrDefault(edgeId, "General Canal") : "General Canal";
            BigDecimal maxCap = edgeId != null ? edgeCapacityMap.getOrDefault(edgeId, new BigDecimal("2.50")) : new BigDecimal("2.50");

            List<SmartIrrigationPlan> upcoming = irrigationPort.getUpcomingPlansForField(f.id());
            BigDecimal requestedVol = BigDecimal.ZERO;
            BigDecimal requiredFlowRate = BigDecimal.ZERO;
            Instant preferredStart = periodStart;
            Instant preferredEnd = periodStart.plusSeconds(14400); // 4 hour delivery window default

            if (!upcoming.isEmpty()) {
                SmartIrrigationPlan plan = upcoming.get(0);
                requestedVol = plan.getRecommendedVolumeM3() != null ? plan.getRecommendedVolumeM3() : new BigDecimal("500.00");
                if (plan.getRecommendedFlowRateM3H() != null) {
                    requiredFlowRate = plan.getRecommendedFlowRateM3H().divide(new BigDecimal("3600"), 4, RoundingMode.HALF_UP);
                } else {
                    requiredFlowRate = new BigDecimal("0.05");
                }
                if (plan.getPlannedStartAt() != null) preferredStart = plan.getPlannedStartAt();
                if (plan.getPlannedEndAt() != null) preferredEnd = plan.getPlannedEndAt();
            } else {
                // Default demand based on area
                requestedVol = f.areaHectares() != null ? f.areaHectares().multiply(new BigDecimal("150.00")) : new BigDecimal("300.00");
                requiredFlowRate = new BigDecimal("0.035");
            }

            BigDecimal remainingQuota = waterPort.getRemainingAllocationVolume(f.id());

            boolean isBlocked = false;
            String blockReason = null;
            if (anomalyPort.hasSuspectedPipeBurst(f.id())) {
                isBlocked = true;
                blockReason = "Hydraulic Safety Interlock: active pipe burst detected on field intake";
            } else if (anomalyPort.hasCriticalPressureDrop(f.id())) {
                isBlocked = true;
                blockReason = "Hydraulic Safety Interlock: critical pressure drop detected";
            }

            demandRequests.add(new CanalDispatchOptimizer.ParcelDemandRequest(
                    f.id(),
                    f.name(),
                    wuaId,
                    targetWuaName,
                    requestedVol,
                    requiredFlowRate,
                    "COTTON".equalsIgnoreCase(f.cropType()) ? 1 : 2,
                    new BigDecimal("42.0"), // Baseline moisture
                    f.cropType(),
                    remainingQuota,
                    edgeId,
                    canalName,
                    maxCap,
                    isBlocked,
                    blockReason,
                    preferredStart,
                    preferredEnd
            ));
        }

        String scheduleTitle = String.format("Canal Dispatch Proposal - %s (%s to %s)",
                targetWuaName, periodStart.toString().substring(0, 10), periodEnd.toString().substring(0, 10));

        CanalDispatchOptimizer.OptimizationResult result = dispatchOptimizer.optimize(
                scheduleTitle,
                targetWuaId,
                targetWuaName,
                demandRequests,
                fairnessPolicy,
                periodStart,
                periodEnd,
                currentUserId
        );

        var savedSchedule = dispatchScheduleRepository.save(result.schedule());
        log.info("Generated and saved canal dispatch schedule: id={}, title='{}', totalAllocatedM3={}, conflictsCount={}",
                savedSchedule.getId(), savedSchedule.getTitle(), savedSchedule.getTotalAllocatedM3(), savedSchedule.getConflicts().size());

        if (!result.detectedConflicts().isEmpty()) {
            notificationPort.sendDispatchNotification(
                    savedSchedule.getId(),
                    "Canal Capacity Conflicts Detected in Dispatch Proposal",
                    String.format("%d multi-parcel conflicts identified during dispatch optimization. Human review required.", result.detectedConflicts().size()),
                    "HIGH"
            );
        }

        return dtoMapper.toScheduleResponse(savedSchedule);
    }
}
