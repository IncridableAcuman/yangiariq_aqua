package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.DistributionEquityAssessment;
import com.aquaai.wateraccounting.domain.model.EquityDisparityLevel;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DistributionEquityEngineTest {

    private DistributionEquityEngine equityEngine;

    @BeforeEach
    void setUp() {
        equityEngine = new DistributionEquityEngine();
    }

    @Test
    @DisplayName("Evaluates balanced cluster (CV <= 0.15) with low variance")
    void evaluatesBalancedCluster() {
        var p1 = new DistributionEquityEngine.ParcelDeliveryData(
                UUID.randomUUID(), "Field A", new BigDecimal("10.0"), new BigDecimal("10000.00"), // 1000 m3/ha
                new BigDecimal("10000.00"), BigDecimal.ZERO, "COTTON", 1
        );
        var p2 = new DistributionEquityEngine.ParcelDeliveryData(
                UUID.randomUUID(), "Field B", new BigDecimal("10.0"), new BigDecimal("10500.00"), // 1050 m3/ha
                new BigDecimal("10000.00"), BigDecimal.ZERO, "COTTON", 1
        );
        var p3 = new DistributionEquityEngine.ParcelDeliveryData(
                UUID.randomUUID(), "Field C", new BigDecimal("10.0"), new BigDecimal("9800.00"), // 980 m3/ha
                new BigDecimal("10000.00"), BigDecimal.ZERO, "COTTON", 1
        );

        DistributionEquityAssessment assessment = equityEngine.evaluateClusterEquity(
                UUID.randomUUID(),
                "Yangiariq Cotton Sector WUA",
                "WUA",
                List.of(p1, p2, p3),
                "CROP_CRITICALITY_FIRST",
                EvidenceClassification.OBSERVED
        );

        assertThat(assessment.getDisparityLevel()).isEqualTo(EquityDisparityLevel.BALANCED);
        assertThat(assessment.getCoefficientOfVariation()).isLessThanOrEqualTo(new BigDecimal("0.15"));
        assertThat(assessment.isConsistentWithPolicy()).isTrue();
    }

    @Test
    @DisplayName("Detects significant disparity when coefficient of variation exceeds 0.35")
    void detectsSignificantDisparity() {
        var p1 = new DistributionEquityEngine.ParcelDeliveryData(
                UUID.randomUUID(), "Head-end Field", new BigDecimal("5.0"), new BigDecimal("10000.00"), // 2000 m3/ha
                new BigDecimal("10000.00"), BigDecimal.ZERO, "WHEAT", 1
        );
        var p2 = new DistributionEquityEngine.ParcelDeliveryData(
                UUID.randomUUID(), "Tail-end Field", new BigDecimal("5.0"), new BigDecimal("2500.00"), // 500 m3/ha
                new BigDecimal("10000.00"), BigDecimal.ZERO, "WHEAT", 1
        );

        DistributionEquityAssessment assessment = equityEngine.evaluateClusterEquity(
                UUID.randomUUID(),
                "Disparate Branch WUA",
                "WUA",
                List.of(p1, p2),
                "CROP_CRITICALITY_FIRST",
                EvidenceClassification.OBSERVED
        );

        assertThat(assessment.getDisparityLevel()).isEqualTo(EquityDisparityLevel.SIGNIFICANT_DISPARITY);
        assertThat(assessment.getCoefficientOfVariation()).isGreaterThan(new BigDecimal("0.35"));
        assertThat(assessment.isConsistentWithPolicy()).isFalse();
    }

    @Test
    @DisplayName("Returns INSUFFICIENT_DATA when sample size is fewer than 2 parcels")
    void returnsInsufficientDataForSingleParcel() {
        var p1 = new DistributionEquityEngine.ParcelDeliveryData(
                UUID.randomUUID(), "Isolated Field", new BigDecimal("5.0"), new BigDecimal("5000.00"),
                new BigDecimal("5000.00"), BigDecimal.ZERO, "COTTON", 1
        );

        DistributionEquityAssessment assessment = equityEngine.evaluateClusterEquity(
                UUID.randomUUID(),
                "Single Parcel Cluster",
                "WUA",
                List.of(p1),
                "CROP_CRITICALITY_FIRST",
                EvidenceClassification.MISSING
        );

        assertThat(assessment.getDisparityLevel()).isEqualTo(EquityDisparityLevel.INSUFFICIENT_DATA);
        assertThat(assessment.getSampleCount()).isEqualTo(1);
    }
}
