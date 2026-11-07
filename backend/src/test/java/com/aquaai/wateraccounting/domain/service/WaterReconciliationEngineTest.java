package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.ReconciliationStatus;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WaterReconciliationEngineTest {

    private WaterReconciliationEngine reconciliationEngine;

    @BeforeEach
    void setUp() {
        reconciliationEngine = new WaterReconciliationEngine();
    }

    @Test
    @DisplayName("Reconciles stage transition when variance is within tolerance")
    void reconcilesWithinTolerance() {
        var input = new WaterReconciliationEngine.BoundaryReconciliationInput(
                "PRIMARY_TO_SECONDARY",
                "Primary to Secondary Branches",
                UUID.randomUUID(),
                Instant.now().minusSeconds(86400 * 7),
                Instant.now(),
                new BigDecimal("100000.00"), // Expected
                new BigDecimal("97000.00"),  // Recorded (-3% variance)
                new BigDecimal("5.00"),      // 5% tolerance
                EvidenceClassification.OBSERVED
        );

        List<WaterReconciliationItem> items = reconciliationEngine.reconcileBoundaries(List.of(input));

        assertThat(items).hasSize(1);
        WaterReconciliationItem item = items.get(0);
        assertThat(item.getStatus()).isEqualTo(ReconciliationStatus.WITHIN_TOLERANCE);
        assertThat(item.getDiscrepancyPercent()).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("Flags discrepancy when variance exceeds tolerance threshold")
    void flagsDiscrepancyWhenExceedingTolerance() {
        var input = new WaterReconciliationEngine.BoundaryReconciliationInput(
                "DELIVERY_VS_CONSUMPTION",
                "Turnout Delivery vs Soil Consumption",
                UUID.randomUUID(),
                Instant.now().minusSeconds(86400 * 7),
                Instant.now(),
                new BigDecimal("50000.00"),
                new BigDecimal("40000.00"),  // -20% variance
                new BigDecimal("8.00"),      // 8% tolerance
                EvidenceClassification.CALCULATED
        );

        List<WaterReconciliationItem> items = reconciliationEngine.reconcileBoundaries(List.of(input));

        assertThat(items).hasSize(1);
        WaterReconciliationItem item = items.get(0);
        assertThat(item.getStatus()).isEqualTo(ReconciliationStatus.DISCREPANCY);
        assertThat(item.getDiscrepancyPercent()).isEqualByComparingTo(new BigDecimal("20.00"));
    }
}
