package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;

public record RegionalWaterOverviewResponse(
        RegionalWaterBalanceResponse districtBalance,
        int totalLedgerEntries,
        int totalReconciliations,
        int unverifiedReconciliationsCount,
        BigDecimal averageConveyanceLossPercent,
        int nonCompliantParcelsCount,
        int activeConservationOpportunitiesCount,
        String equityStatusSummary
) {}
