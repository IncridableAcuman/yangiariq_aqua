package com.aquaai.network.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RegionalDemandResponse(
        BigDecimal districtTotalRequestedM3,
        BigDecimal districtTotalAllocatedM3,
        BigDecimal districtTotalUnmetM3,
        List<WuaDemandItem> wuaDemands,
        List<CanalDemandItem> canalDemands
) {
    public record WuaDemandItem(
            UUID wuaId,
            String wuaName,
            BigDecimal requestedM3,
            BigDecimal allocatedM3,
            BigDecimal unmetM3,
            int fieldCount
    ) {}

    public record CanalDemandItem(
            UUID edgeId,
            String canalName,
            BigDecimal totalDemandM3,
            BigDecimal canalCapacityM3S,
            boolean isConstrained
    ) {}
}
