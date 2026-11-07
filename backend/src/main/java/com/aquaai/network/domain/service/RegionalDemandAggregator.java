package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.RegionalWaterDemand;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Multi-parcel demand aggregator.
 * Summarizes volumetric requirements across fields without double counting.
 */
public class RegionalDemandAggregator {

    public record ParcelDemandInput(
            UUID fieldId,
            String fieldName,
            UUID wuaId,
            String wuaName,
            UUID supplyEdgeId,
            String canalName,
            BigDecimal requestedVolumeM3,
            BigDecimal allocatedVolumeM3,
            BigDecimal deliveredVolumeM3,
            BigDecimal upcomingVolumeM3,
            BigDecimal requiredFlowRateM3S
    ) {}

    public RegionalWaterDemand aggregateDistrictDemand(
            String districtName,
            List<ParcelDemandInput> inputs,
            Instant windowStart,
            Instant windowEnd
    ) {
        BigDecimal totalRequested = BigDecimal.ZERO;
        BigDecimal totalAllocated = BigDecimal.ZERO;
        BigDecimal totalDelivered = BigDecimal.ZERO;
        BigDecimal totalUpcoming = BigDecimal.ZERO;
        BigDecimal peakFlow = BigDecimal.ZERO;

        if (inputs != null) {
            for (ParcelDemandInput p : inputs) {
                totalRequested = totalRequested.add(p.requestedVolumeM3() != null ? p.requestedVolumeM3() : BigDecimal.ZERO);
                totalAllocated = totalAllocated.add(p.allocatedVolumeM3() != null ? p.allocatedVolumeM3() : BigDecimal.ZERO);
                totalDelivered = totalDelivered.add(p.deliveredVolumeM3() != null ? p.deliveredVolumeM3() : BigDecimal.ZERO);
                totalUpcoming = totalUpcoming.add(p.upcomingVolumeM3() != null ? p.upcomingVolumeM3() : BigDecimal.ZERO);
                peakFlow = peakFlow.add(p.requiredFlowRateM3S() != null ? p.requiredFlowRateM3S() : BigDecimal.ZERO);
            }
        }

        return RegionalWaterDemand.builder()
                .targetLevel("DISTRICT")
                .entityId(UUID.randomUUID())
                .entityName(districtName != null ? districtName : "Yangiariq District")
                .totalRequestedVolumeM3(totalRequested)
                .totalAllocatedVolumeM3(totalAllocated)
                .totalDeliveredVolumeM3(totalDelivered)
                .totalUpcomingVolumeM3(totalUpcoming)
                .activeParcelCount(inputs != null ? inputs.size() : 0)
                .peakFlowRateM3S(peakFlow)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();
    }

    public List<RegionalWaterDemand> aggregateWuaDemands(
            List<ParcelDemandInput> inputs,
            Instant windowStart,
            Instant windowEnd
    ) {
        Map<UUID, List<ParcelDemandInput>> wuaGroups = new HashMap<>();
        Map<UUID, String> wuaNames = new HashMap<>();

        if (inputs != null) {
            for (ParcelDemandInput p : inputs) {
                UUID wuaId = p.wuaId() != null ? p.wuaId() : UUID.fromString("00000000-0000-0000-0000-000000000000");
                wuaGroups.computeIfAbsent(wuaId, k -> new ArrayList<>()).add(p);
                wuaNames.put(wuaId, p.wuaName() != null ? p.wuaName() : "General WUA");
            }
        }

        List<RegionalWaterDemand> results = new ArrayList<>();
        for (Map.Entry<UUID, List<ParcelDemandInput>> entry : wuaGroups.entrySet()) {
            UUID wuaId = entry.getKey();
            List<ParcelDemandInput> parcelList = entry.getValue();

            BigDecimal totalReq = BigDecimal.ZERO;
            BigDecimal totalAlloc = BigDecimal.ZERO;
            BigDecimal totalDeliv = BigDecimal.ZERO;
            BigDecimal totalUp = BigDecimal.ZERO;
            BigDecimal peakFlow = BigDecimal.ZERO;

            for (ParcelDemandInput p : parcelList) {
                totalReq = totalReq.add(p.requestedVolumeM3() != null ? p.requestedVolumeM3() : BigDecimal.ZERO);
                totalAlloc = totalAlloc.add(p.allocatedVolumeM3() != null ? p.allocatedVolumeM3() : BigDecimal.ZERO);
                totalDeliv = totalDeliv.add(p.deliveredVolumeM3() != null ? p.deliveredVolumeM3() : BigDecimal.ZERO);
                totalUp = totalUp.add(p.upcomingVolumeM3() != null ? p.upcomingVolumeM3() : BigDecimal.ZERO);
                peakFlow = peakFlow.add(p.requiredFlowRateM3S() != null ? p.requiredFlowRateM3S() : BigDecimal.ZERO);
            }

            results.add(RegionalWaterDemand.builder()
                    .targetLevel("WUA")
                    .entityId(wuaId)
                    .entityName(wuaNames.get(wuaId))
                    .totalRequestedVolumeM3(totalReq)
                    .totalAllocatedVolumeM3(totalAlloc)
                    .totalDeliveredVolumeM3(totalDeliv)
                    .totalUpcomingVolumeM3(totalUp)
                    .activeParcelCount(parcelList.size())
                    .peakFlowRateM3S(peakFlow)
                    .windowStart(windowStart)
                    .windowEnd(windowEnd)
                    .build());
        }

        return results;
    }
}
