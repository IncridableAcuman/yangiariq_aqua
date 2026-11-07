package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.AllocationComplianceItem;
import com.aquaai.wateraccounting.domain.model.AllocationComplianceStatus;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationComplianceEngineTest {

    private AllocationComplianceEngine complianceEngine;

    @BeforeEach
    void setUp() {
        complianceEngine = new AllocationComplianceEngine();
    }

    @Test
    @DisplayName("Marks status as ALLOCATION_RESPECTED when consumption is within limits")
    void marksAllocationRespected() {
        AllocationComplianceItem item = complianceEngine.evaluateCompliance(
                UUID.randomUUID(),
                "Parcel Cotton #1",
                "FIELD",
                "Yangiariq WUA",
                "COTTON",
                new BigDecimal("5.0"),
                new BigDecimal("6000.00"), // Allocated
                BigDecimal.ZERO,
                new BigDecimal("4500.00"), // Approved dispatch
                new BigDecimal("4400.00"), // Delivered
                new BigDecimal("4000.00"), // Consumed
                EvidenceClassification.OBSERVED
        );

        assertThat(item.getStatus()).isEqualTo(AllocationComplianceStatus.ALLOCATION_RESPECTED);
        assertThat(item.getRemainingAllocationM3()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(item.getOverDeliveryM3()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Marks status as ALLOCATION_EXCEEDED when consumption surpasses quota")
    void marksAllocationExceeded() {
        AllocationComplianceItem item = complianceEngine.evaluateCompliance(
                UUID.randomUUID(),
                "Parcel Wheat #2",
                "FIELD",
                "Bog'ot WUA",
                "WHEAT",
                new BigDecimal("4.0"),
                new BigDecimal("4000.00"), // Allocated
                BigDecimal.ZERO,
                new BigDecimal("4500.00"),
                new BigDecimal("4800.00"),
                new BigDecimal("4600.00"), // Consumed > 4000
                EvidenceClassification.OBSERVED
        );

        assertThat(item.getStatus()).isEqualTo(AllocationComplianceStatus.ALLOCATION_EXCEEDED);
        assertThat(item.getRemainingAllocationM3()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Marks status as UNDER_DELIVERED when delivery is less than 80% of approved dispatch")
    void marksUnderDelivered() {
        AllocationComplianceItem item = complianceEngine.evaluateCompliance(
                UUID.randomUUID(),
                "Parcel Orchard #3",
                "FIELD",
                "Yangiariq WUA",
                "ORCHARD",
                new BigDecimal("3.0"),
                new BigDecimal("5000.00"),
                BigDecimal.ZERO,
                new BigDecimal("4000.00"), // Approved dispatch
                new BigDecimal("2500.00"), // Delivered < 3200 (80%)
                new BigDecimal("2200.00"),
                EvidenceClassification.OBSERVED
        );

        assertThat(item.getStatus()).isEqualTo(AllocationComplianceStatus.UNDER_DELIVERED);
        assertThat(item.getUnderDeliveryM3()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Handles zero allocation safely as INSUFFICIENT_DATA")
    void handlesZeroAllocation() {
        AllocationComplianceItem item = complianceEngine.evaluateCompliance(
                UUID.randomUUID(),
                "Unregistered Parcel",
                "FIELD",
                "Unknown WUA",
                "OTHER",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                EvidenceClassification.MISSING
        );

        assertThat(item.getStatus()).isEqualTo(AllocationComplianceStatus.INSUFFICIENT_DATA);
    }
}
