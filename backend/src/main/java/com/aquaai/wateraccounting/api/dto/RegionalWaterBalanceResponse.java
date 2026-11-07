package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RegionalWaterBalanceResponse(
        String boundaryDimension,
        String boundaryName,
        UUID boundaryId,
        String periodType,
        Instant periodStart,
        Instant periodEnd,
        BigDecimal openingBalanceM3,
        BigDecimal inflowReceivedM3,
        BigDecimal waterTransferredInM3,
        BigDecimal waterTransferredOutM3,
        BigDecimal waterDeliveredM3,
        BigDecimal waterConsumedM3,
        BigDecimal verifiedLossesM3,
        BigDecimal recordedReturnFlowM3,
        BigDecimal closingBalanceM3,
        BigDecimal discrepancyM3,
        BigDecimal tolerancePercent,
        boolean isReconciled,
        String evidenceClassification,
        BigDecimal dataCompletenessPercent,
        String notes
) {}
