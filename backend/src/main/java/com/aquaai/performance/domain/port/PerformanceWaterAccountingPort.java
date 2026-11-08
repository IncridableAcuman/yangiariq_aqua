package com.aquaai.performance.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PerformanceWaterAccountingPort {

    record ConveyanceLossSummary(
            UUID edgeId,
            String canalName,
            BigDecimal upstreamVolumeM3,
            BigDecimal downstreamVolumeM3,
            BigDecimal lossVolumeM3,
            BigDecimal lossPercentage,
            String classification
    ) {}

    record AllocationComplianceSummary(
            long totalEntities,
            long compliantEntities,
            long exceededEntities,
            long underDeliveredEntities,
            BigDecimal averageQuotaUtilizationPct
    ) {}

    record DistributionEquitySummary(
            BigDecimal coefficientOfVariation,
            String disparityLevel,
            BigDecimal meanDeliveryPerHectareM3
    ) {}

    List<ConveyanceLossSummary> getRecentConveyanceLosses();
    AllocationComplianceSummary getAllocationComplianceSummary();
    DistributionEquitySummary getRegionalDistributionEquity();
    BigDecimal getTotalDeliveredWaterM3();
    BigDecimal getVerifiedConservationSavingsM3();
}
