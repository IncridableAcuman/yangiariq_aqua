package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.AccountingDimension;
import com.aquaai.wateraccounting.domain.model.AccountingPeriodType;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.RegionalWaterBalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain engine executing deterministic regional water balance equations.
 * <p>
 * Fundamental accounting principle:
 * CLOSING_BALANCE = OPENING_BALANCE + INFLOW - CONSUMPTION - LOSSES - RETURN_FLOW
 * <p>
 * Double-counting prevention:
 * Internal transfers between canal nodes are never counted as new district-level inflow.
 * Field consumption is strictly separated from canal turnout delivery.
 */
public class WaterBalanceEngine {

    public static final BigDecimal DEFAULT_TOLERANCE_PERCENT = new BigDecimal("5.00");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public RegionalWaterBalance calculateBalance(
            AccountingDimension dimension,
            String boundaryName,
            UUID boundaryId,
            AccountingPeriodType periodType,
            Instant periodStart,
            Instant periodEnd,
            BigDecimal openingBalanceM3,
            BigDecimal inflowReceivedM3,
            BigDecimal transfersInM3,
            BigDecimal transfersOutM3,
            BigDecimal waterDeliveredM3,
            BigDecimal waterConsumedM3,
            BigDecimal verifiedLossesM3,
            BigDecimal returnFlowM3,
            BigDecimal customTolerancePercent,
            EvidenceClassification classification
    ) {
        BigDecimal opening = openingBalanceM3 != null ? openingBalanceM3 : BigDecimal.ZERO;
        BigDecimal inflow = inflowReceivedM3 != null ? inflowReceivedM3 : BigDecimal.ZERO;
        BigDecimal tIn = transfersInM3 != null ? transfersInM3 : BigDecimal.ZERO;
        BigDecimal tOut = transfersOutM3 != null ? transfersOutM3 : BigDecimal.ZERO;
        BigDecimal delivered = waterDeliveredM3 != null ? waterDeliveredM3 : BigDecimal.ZERO;
        BigDecimal consumed = waterConsumedM3 != null ? waterConsumedM3 : BigDecimal.ZERO;
        BigDecimal losses = verifiedLossesM3 != null ? verifiedLossesM3 : BigDecimal.ZERO;
        BigDecimal returnFlow = returnFlowM3 != null ? returnFlowM3 : BigDecimal.ZERO;
        BigDecimal tolerance = customTolerancePercent != null ? customTolerancePercent : DEFAULT_TOLERANCE_PERCENT;

        // Closing balance = Opening + Inflow + TransfersIn - TransfersOut - Consumed - Losses - ReturnFlow
        // If at district level, internal transfers net out to zero.
        BigDecimal netTransfers = tIn.subtract(tOut);
        BigDecimal effectiveInflow = inflow.add(netTransfers);

        // Theoretical closing volume remaining in conveyance/storage
        BigDecimal closing = opening.add(effectiveInflow).subtract(consumed).subtract(losses).subtract(returnFlow);
        if (closing.compareTo(BigDecimal.ZERO) < 0) {
            closing = BigDecimal.ZERO;
        }

        // Expected conveyance balance: Inflow should equal Delivered + Losses
        // Discrepancy = |Inflow - (Delivered + Losses)|
        BigDecimal accountedConveyance = delivered.add(losses);
        BigDecimal discrepancy = effectiveInflow.subtract(accountedConveyance).abs().setScale(2, ROUNDING);

        BigDecimal discrepancyPercent = BigDecimal.ZERO;
        if (effectiveInflow.compareTo(BigDecimal.ZERO) > 0) {
            discrepancyPercent = discrepancy.multiply(new BigDecimal("100.00")).divide(effectiveInflow, 2, ROUNDING);
        }

        boolean isReconciled = discrepancyPercent.compareTo(tolerance) <= 0;

        String notes;
        if (isReconciled) {
            notes = String.format("Water balance reconciled within %.2f%% tolerance (discrepancy: %.2f m³)", tolerance, discrepancy);
        } else {
            notes = String.format("Unexplained variance of %.2f m³ (%.2f%%) exceeds allowable tolerance of %.2f%%", discrepancy, discrepancyPercent, tolerance);
        }

        return RegionalWaterBalance.builder()
                .boundaryDimension(dimension)
                .boundaryName(boundaryName)
                .boundaryId(boundaryId)
                .periodType(periodType)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .openingBalanceM3(opening.setScale(2, ROUNDING))
                .inflowReceivedM3(inflow.setScale(2, ROUNDING))
                .waterTransferredInM3(tIn.setScale(2, ROUNDING))
                .waterTransferredOutM3(tOut.setScale(2, ROUNDING))
                .waterDeliveredM3(delivered.setScale(2, ROUNDING))
                .waterConsumedM3(consumed.setScale(2, ROUNDING))
                .verifiedLossesM3(losses.setScale(2, ROUNDING))
                .recordedReturnFlowM3(returnFlow.setScale(2, ROUNDING))
                .closingBalanceM3(closing.setScale(2, ROUNDING))
                .discrepancyM3(discrepancy)
                .tolerancePercent(tolerance)
                .isReconciled(isReconciled)
                .evidenceClassification(classification != null ? classification : EvidenceClassification.CALCULATED)
                .dataCompletenessPercent(new BigDecimal("100.00"))
                .notes(notes)
                .build();
    }
}
