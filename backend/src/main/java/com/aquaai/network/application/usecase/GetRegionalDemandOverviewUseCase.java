package com.aquaai.network.application.usecase;

import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.network.api.dto.RegionalDemandResponse;
import com.aquaai.network.domain.model.RegionalWaterDemand;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.port.NetworkFieldPort;
import com.aquaai.network.domain.port.NetworkIrrigationPort;
import com.aquaai.network.domain.port.NetworkWaterPort;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import com.aquaai.network.domain.service.RegionalDemandAggregator;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class GetRegionalDemandOverviewUseCase {

    private final NetworkFieldPort fieldPort;
    private final NetworkWaterPort waterPort;
    private final NetworkIrrigationPort irrigationPort;
    private final WaterNetworkNodeRepository nodeRepository;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterUserAssociationRepository wuaRepository;
    private final RegionalDemandAggregator demandAggregator;

    @Transactional(readOnly = true)
    public RegionalDemandResponse execute() {
        List<NetworkFieldPort.FieldParcelInfo> fields = fieldPort.getAllFields();
        List<WaterNetworkNode> nodes = nodeRepository.findAll();
        List<WaterNetworkEdge> edges = edgeRepository.findAll();
        List<WaterUserAssociation> wuas = wuaRepository.findAll();

        Map<UUID, UUID> fieldToWuaId = new HashMap<>();
        Map<UUID, String> wuaNameMap = new HashMap<>();
        for (WaterUserAssociation wua : wuas) {
            wuaNameMap.put(wua.getId(), wua.getName());
        }

        Map<UUID, UUID> fieldToEdgeId = new HashMap<>();
        Map<UUID, String> edgeNameMap = new HashMap<>();
        for (WaterNetworkEdge edge : edges) {
            edgeNameMap.put(edge.getId(), edge.getName());
        }

        // Map field inlet nodes to supply edges
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

        List<RegionalDemandAggregator.ParcelDemandInput> parcelInputs = new ArrayList<>();
        Instant now = Instant.now();
        Instant windowEnd = now.plusSeconds(86400 * 7);

        for (NetworkFieldPort.FieldParcelInfo f : fields) {
            UUID wuaId = fieldToWuaId.get(f.id());
            String wuaName = wuaId != null ? wuaNameMap.getOrDefault(wuaId, "General WUA") : "General WUA";
            UUID edgeId = fieldToEdgeId.get(f.id());
            String canalName = edgeId != null ? edgeNameMap.getOrDefault(edgeId, "Secondary Canal") : "Secondary Canal";

            List<SmartIrrigationPlan> upcoming = irrigationPort.getUpcomingPlansForField(f.id());
            BigDecimal requestedVol = BigDecimal.ZERO;
            BigDecimal requiredFlowRate = BigDecimal.ZERO;
            for (SmartIrrigationPlan plan : upcoming) {
                if (plan.getRecommendedVolumeM3() != null) {
                    requestedVol = requestedVol.add(plan.getRecommendedVolumeM3());
                }
                if (plan.getRecommendedFlowRateM3H() != null) {
                    // convert m3/h to m3/s
                    requiredFlowRate = requiredFlowRate.add(plan.getRecommendedFlowRateM3H().divide(new BigDecimal("3600"), 4, RoundingMode.HALF_UP));
                }
            }

            BigDecimal remainingAllocation = waterPort.getRemainingAllocationVolume(f.id());
            BigDecimal allocatedVol = remainingAllocation.min(requestedVol);

            parcelInputs.add(new RegionalDemandAggregator.ParcelDemandInput(
                    f.id(),
                    f.name(),
                    wuaId,
                    wuaName,
                    edgeId,
                    canalName,
                    requestedVol,
                    allocatedVol,
                    BigDecimal.ZERO,
                    requestedVol,
                    requiredFlowRate
            ));
        }

        RegionalWaterDemand districtDemand = demandAggregator.aggregateDistrictDemand("Yangiariq District", parcelInputs, now, windowEnd);
        List<RegionalWaterDemand> wuaDemandsList = demandAggregator.aggregateWuaDemands(parcelInputs, now, windowEnd);

        List<RegionalDemandResponse.WuaDemandItem> wuaItems = wuaDemandsList.stream()
                .map(d -> new RegionalDemandResponse.WuaDemandItem(
                        d.entityId(),
                        d.entityName(),
                        d.totalRequestedVolumeM3(),
                        d.totalAllocatedVolumeM3(),
                        d.totalRequestedVolumeM3().subtract(d.totalAllocatedVolumeM3()),
                        d.activeParcelCount()
                ))
                .toList();

        // Canal demand overview
        Map<UUID, BigDecimal> canalDemandMap = new HashMap<>();
        for (RegionalDemandAggregator.ParcelDemandInput p : parcelInputs) {
            if (p.supplyEdgeId() != null) {
                canalDemandMap.merge(p.supplyEdgeId(), p.requestedVolumeM3(), BigDecimal::add);
            }
        }

        List<RegionalDemandResponse.CanalDemandItem> canalItems = new ArrayList<>();
        for (WaterNetworkEdge edge : edges) {
            BigDecimal demand = canalDemandMap.getOrDefault(edge.getId(), BigDecimal.ZERO);
            boolean isConstrained = false;
            if (edge.getMaxCapacityM3S() != null && edge.getMaxCapacityM3S().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal hourlyCapacity = edge.getMaxCapacityM3S().multiply(new BigDecimal("3600"));
                if (demand.compareTo(hourlyCapacity.multiply(new BigDecimal("24"))) > 0) {
                    isConstrained = true;
                }
            }
            canalItems.add(new RegionalDemandResponse.CanalDemandItem(
                    edge.getId(),
                    edge.getName(),
                    demand,
                    edge.getMaxCapacityM3S(),
                    isConstrained
            ));
        }

        return new RegionalDemandResponse(
                districtDemand.totalRequestedVolumeM3(),
                districtDemand.totalAllocatedVolumeM3(),
                districtDemand.totalRequestedVolumeM3().subtract(districtDemand.totalAllocatedVolumeM3()),
                wuaItems,
                canalItems
        );
    }
}
