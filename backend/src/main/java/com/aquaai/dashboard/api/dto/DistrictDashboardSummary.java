package com.aquaai.dashboard.api.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DistrictDashboardSummary(
        String district,
        long totalRegisteredFarmers,
        long totalMonitoredFields,
        BigDecimal totalCultivatedHectares,
        BigDecimal totalWaterDistributedM3,
        long activeWaterProblems,
        long activeSensorsOnline
) {
}
