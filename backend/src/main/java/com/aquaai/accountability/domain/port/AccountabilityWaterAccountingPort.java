package com.aquaai.accountability.domain.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AccountabilityWaterAccountingPort {

    record DiscrepancyFinding(
            UUID reconciliationId,
            String boundaryType,
            String boundaryName,
            UUID targetId,
            BigDecimal discrepancyVolumeM3,
            BigDecimal discrepancyPercent,
            String explanation,
            Instant detectedAt
    ) {}

    record LossFinding(
            UUID edgeId,
            String canalName,
            BigDecimal lossVolumeM3,
            BigDecimal lossPercentage,
            String classification,
            Instant detectedAt
    ) {}

    List<DiscrepancyFinding> findPendingDiscrepancies();

    List<LossFinding> findSignificantConveyanceLosses();
}
