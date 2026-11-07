package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.AccountingDimension;
import com.aquaai.wateraccounting.domain.model.AccountingPeriodType;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.RegionalWaterBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WaterBalanceEngineTest {

    private WaterBalanceEngine balanceEngine;

    @BeforeEach
    void setUp() {
        balanceEngine = new WaterBalanceEngine();
    }

    @Test
    @DisplayName("Calculates exact water balance when inflows match delivered and losses")
    void calculatesExactReconciledBalance() {
        Instant start = Instant.now().minusSeconds(86400 * 30);
        Instant end = Instant.now();

        BigDecimal opening = new BigDecimal("10000.00");
        BigDecimal inflow = new BigDecimal("100000.00");
        BigDecimal delivered = new BigDecimal("92000.00");
        BigDecimal losses = new BigDecimal("8000.00");
        BigDecimal consumed = new BigDecimal("85000.00");
        BigDecimal returnFlow = new BigDecimal("3000.00");

        RegionalWaterBalance balance = balanceEngine.calculateBalance(
                AccountingDimension.DISTRICT,
                "Yangiariq District",
                UUID.randomUUID(),
                AccountingPeriodType.SEASONAL,
                start,
                end,
                opening,
                inflow,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                delivered,
                consumed,
                losses,
                returnFlow,
                new BigDecimal("5.00"),
                EvidenceClassification.OBSERVED
        );

        assertThat(balance).isNotNull();
        assertThat(balance.isReconciled()).isTrue();
        assertThat(balance.getDiscrepancyM3()).isEqualByComparingTo(BigDecimal.ZERO);
        // Closing = 10000 + 100000 - 85000 - 8000 - 3000 = 14000
        assertThat(balance.getClosingBalanceM3()).isEqualByComparingTo(new BigDecimal("14000.00"));
        assertThat(balance.getInflowReceivedM3()).isEqualByComparingTo(inflow);
        assertThat(balance.getWaterDeliveredM3()).isEqualByComparingTo(delivered);
    }

    @Test
    @DisplayName("Detects significant discrepancy when unaccounted volume exceeds allowable tolerance")
    void detectsDiscrepancyExceedingTolerance() {
        Instant start = Instant.now().minusSeconds(86400 * 30);
        Instant end = Instant.now();

        BigDecimal opening = new BigDecimal("5000.00");
        BigDecimal inflow = new BigDecimal("100000.00");
        BigDecimal delivered = new BigDecimal("70000.00");
        BigDecimal losses = new BigDecimal("10000.00"); // 100000 - (70000 + 10000) = 20000 discrepancy (20%)
        BigDecimal consumed = new BigDecimal("65000.00");

        RegionalWaterBalance balance = balanceEngine.calculateBalance(
                AccountingDimension.DISTRICT,
                "Yangiariq District",
                null,
                AccountingPeriodType.MONTHLY,
                start,
                end,
                opening,
                inflow,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                delivered,
                consumed,
                losses,
                BigDecimal.ZERO,
                new BigDecimal("5.00"),
                EvidenceClassification.CALCULATED
        );

        assertThat(balance.isReconciled()).isFalse();
        assertThat(balance.getDiscrepancyM3()).isEqualByComparingTo(new BigDecimal("20000.00"));
        assertThat(balance.getNotes()).containsIgnoringCase("exceeds allowable tolerance");
    }

    @Test
    @DisplayName("Internal network transfers net out properly without double counting")
    void internalTransfersNetOutProperly() {
        BigDecimal inflow = new BigDecimal("50000.00");
        BigDecimal tIn = new BigDecimal("20000.00");
        BigDecimal tOut = new BigDecimal("20000.00"); // Net transfers = 0

        RegionalWaterBalance balance = balanceEngine.calculateBalance(
                AccountingDimension.PRIMARY_CANAL,
                "Shavat Canal",
                UUID.randomUUID(),
                AccountingPeriodType.WEEKLY,
                Instant.now().minusSeconds(86400 * 7),
                Instant.now(),
                BigDecimal.ZERO,
                inflow,
                tIn,
                tOut,
                new BigDecimal("46000.00"),
                new BigDecimal("40000.00"),
                new BigDecimal("4000.00"),
                BigDecimal.ZERO,
                new BigDecimal("5.00"),
                EvidenceClassification.CALCULATED
        );

        assertThat(balance.isReconciled()).isTrue();
        assertThat(balance.getWaterTransferredInM3()).isEqualByComparingTo(tIn);
        assertThat(balance.getWaterTransferredOutM3()).isEqualByComparingTo(tOut);
    }
}
