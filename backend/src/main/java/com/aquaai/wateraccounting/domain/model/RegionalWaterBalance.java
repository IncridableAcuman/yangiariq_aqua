package com.aquaai.wateraccounting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Deterministic regional water balance aggregate for a designated boundary and accounting period.
 */
@Getter
@Builder(toBuilder = true)
public class RegionalWaterBalance {

    private final AccountingDimension boundaryDimension;
    private final String boundaryName;
    private final UUID boundaryId;
    private final AccountingPeriodType periodType;
    private final Instant periodStart;
    private final Instant periodEnd;

    private final BigDecimal openingBalanceM3;
    private final BigDecimal inflowReceivedM3;
    private final BigDecimal waterTransferredInM3;
    private final BigDecimal waterTransferredOutM3;
    private final BigDecimal waterDeliveredM3;
    private final BigDecimal waterConsumedM3;
    private final BigDecimal verifiedLossesM3;
    private final BigDecimal recordedReturnFlowM3;
    private final BigDecimal closingBalanceM3;
    private final BigDecimal discrepancyM3;
    private final BigDecimal tolerancePercent;
    private final boolean isReconciled;
    private final EvidenceClassification evidenceClassification;
    private final BigDecimal dataCompletenessPercent;
    private final String notes;

    public RegionalWaterBalance(
            AccountingDimension boundaryDimension,
            String boundaryName,
            UUID boundaryId,
            AccountingPeriodType periodType,
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
            EvidenceClassification evidenceClassification,
            BigDecimal dataCompletenessPercent,
            String notes
    ) {
        this.boundaryDimension = boundaryDimension != null ? boundaryDimension : AccountingDimension.DISTRICT;
        this.boundaryName = boundaryName != null ? boundaryName : "Yangiariq District";
        this.boundaryId = boundaryId;
        this.periodType = periodType != null ? periodType : AccountingPeriodType.SEASONAL;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.openingBalanceM3 = openingBalanceM3 != null ? openingBalanceM3 : BigDecimal.ZERO;
        this.inflowReceivedM3 = inflowReceivedM3 != null ? inflowReceivedM3 : BigDecimal.ZERO;
        this.waterTransferredInM3 = waterTransferredInM3 != null ? waterTransferredInM3 : BigDecimal.ZERO;
        this.waterTransferredOutM3 = waterTransferredOutM3 != null ? waterTransferredOutM3 : BigDecimal.ZERO;
        this.waterDeliveredM3 = waterDeliveredM3 != null ? waterDeliveredM3 : BigDecimal.ZERO;
        this.waterConsumedM3 = waterConsumedM3 != null ? waterConsumedM3 : BigDecimal.ZERO;
        this.verifiedLossesM3 = verifiedLossesM3 != null ? verifiedLossesM3 : BigDecimal.ZERO;
        this.recordedReturnFlowM3 = recordedReturnFlowM3 != null ? recordedReturnFlowM3 : BigDecimal.ZERO;
        this.closingBalanceM3 = closingBalanceM3 != null ? closingBalanceM3 : BigDecimal.ZERO;
        this.discrepancyM3 = discrepancyM3 != null ? discrepancyM3 : BigDecimal.ZERO;
        this.tolerancePercent = tolerancePercent != null ? tolerancePercent : new BigDecimal("5.00");
        this.isReconciled = isReconciled;
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.dataCompletenessPercent = dataCompletenessPercent != null ? dataCompletenessPercent : new BigDecimal("100.00");
        this.notes = notes;
    }
}
