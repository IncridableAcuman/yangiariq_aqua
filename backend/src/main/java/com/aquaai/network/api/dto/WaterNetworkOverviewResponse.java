package com.aquaai.network.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record WaterNetworkOverviewResponse(
        int totalNodes,
        int totalEdges,
        int operationalEdges,
        int constrainedEdges,
        int blockedEdges,
        BigDecimal totalConveyanceCapacityM3S,
        BigDecimal totalCurrentFlowM3S,
        BigDecimal overallNetworkUtilizationPercent,
        int activeScheduleCount,
        String latestActiveScheduleTitle,
        String latestActiveScheduleStatus,
        List<String> recentSurgeRiskWarnings
) {}
