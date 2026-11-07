package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.ConveyanceLossAssessment;
import com.aquaai.wateraccounting.domain.model.ConveyanceLossClassification;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConveyanceLossEngineTest {

    private ConveyanceLossEngine lossEngine;

    @BeforeEach
    void setUp() {
        lossEngine = new ConveyanceLossEngine();
    }

    @Test
    @DisplayName("Classifies minimal loss (<= 5%) as NO_SIGNIFICANT_DISCREPANCY")
    void classifiesOptimalConveyance() {
        ConveyanceLossAssessment assessment = lossEngine.assessSegmentLoss(
                UUID.randomUUID(),
                "Primary Flume Canal",
                "PRIMARY_CANAL",
                "Headgate A",
                "Tailgate B",
                Instant.now().minusSeconds(86400),
                Instant.now(),
                new BigDecimal("10000.00"),
                new BigDecimal("9650.00"), // 3.5% loss
                EvidenceClassification.OBSERVED,
                "HIGH"
        );

        assertThat(assessment.getClassification()).isEqualTo(ConveyanceLossClassification.NO_SIGNIFICANT_DISCREPANCY);
        assertThat(assessment.getLossVolumeM3()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(assessment.getLossPercentage()).isEqualByComparingTo(new BigDecimal("3.50"));
    }

    @Test
    @DisplayName("Classifies standard loss (5-15%) as VERIFIED_LOSS")
    void classifiesStandardLoss() {
        ConveyanceLossAssessment assessment = lossEngine.assessSegmentLoss(
                UUID.randomUUID(),
                "Polvon Secondary Earthen Canal",
                "SECONDARY_CANAL",
                "Node 1",
                "Node 2",
                Instant.now().minusSeconds(86400),
                Instant.now(),
                new BigDecimal("50000.00"),
                new BigDecimal("46000.00"), // 8% loss
                EvidenceClassification.OBSERVED,
                "HIGH"
        );

        assertThat(assessment.getClassification()).isEqualTo(ConveyanceLossClassification.VERIFIED_LOSS);
        assertThat(assessment.getLossPercentage()).isEqualByComparingTo(new BigDecimal("8.00"));
    }

    @Test
    @DisplayName("Classifies high loss (15-30%) as SUSPECTED_LOSS")
    void classifiesSuspectedLoss() {
        ConveyanceLossAssessment assessment = lossEngine.assessSegmentLoss(
                UUID.randomUUID(),
                "Unlined Lateral Ditch",
                "DISTRIBUTION_CHANNEL",
                "Node 3",
                "Node 4",
                Instant.now().minusSeconds(86400),
                Instant.now(),
                new BigDecimal("20000.00"),
                new BigDecimal("15500.00"), // 22.5% loss
                EvidenceClassification.CALCULATED,
                "HIGH"
        );

        assertThat(assessment.getClassification()).isEqualTo(ConveyanceLossClassification.SUSPECTED_LOSS);
        assertThat(assessment.getLossPercentage()).isEqualByComparingTo(new BigDecimal("22.50"));
    }

    @Test
    @DisplayName("Classifies extreme loss (> 30%) as UNACCOUNTED_WITHDRAWAL")
    void classifiesUnaccountedWithdrawal() {
        ConveyanceLossAssessment assessment = lossEngine.assessSegmentLoss(
                UUID.randomUUID(),
                "Bog'ot Canal Branch",
                "SECONDARY_CANAL",
                "Node 5",
                "Node 6",
                Instant.now().minusSeconds(86400),
                Instant.now(),
                new BigDecimal("30000.00"),
                new BigDecimal("18000.00"), // 40% loss
                EvidenceClassification.CALCULATED,
                "HIGH"
        );

        assertThat(assessment.getClassification()).isEqualTo(ConveyanceLossClassification.UNACCOUNTED_WITHDRAWAL);
        assertThat(assessment.getLossPercentage()).isEqualByComparingTo(new BigDecimal("40.00"));
    }

    @Test
    @DisplayName("Handles zero upstream volume gracefully as INSUFFICIENT_DATA")
    void handlesZeroUpstreamGracefully() {
        ConveyanceLossAssessment assessment = lossEngine.assessSegmentLoss(
                UUID.randomUUID(),
                "Dry Canal Segment",
                "PRIMARY_CANAL",
                "Node 7",
                "Node 8",
                Instant.now().minusSeconds(86400),
                Instant.now(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                EvidenceClassification.MISSING,
                "LOW"
        );

        assertThat(assessment.getClassification()).isEqualTo(ConveyanceLossClassification.INSUFFICIENT_DATA);
        assertThat(assessment.getLossVolumeM3()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
