package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.AllocationComplianceItem;
import com.aquaai.wateraccounting.domain.model.AllocationComplianceStatus;
import com.aquaai.wateraccounting.domain.model.ConservationOpportunity;
import com.aquaai.wateraccounting.domain.model.ConveyanceLossAssessment;
import com.aquaai.wateraccounting.domain.model.ConveyanceLossClassification;
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

class ConservationIntelligenceEngineTest {

    private ConservationIntelligenceEngine conservationEngine;

    @BeforeEach
    void setUp() {
        conservationEngine = new ConservationIntelligenceEngine();
    }

    @Test
    @DisplayName("Synthesizes canal lining opportunity with physical recovery calculation")
    void synthesizesCanalLiningOpportunity() {
        var loss = ConveyanceLossAssessment.builder()
                .edgeId(UUID.randomUUID())
                .canalName("Shavat Main Canal")
                .edgeType("PRIMARY_CANAL")
                .lossVolumeM3(new BigDecimal("20000.00"))
                .lossPercentage(new BigDecimal("22.00"))
                .classification(ConveyanceLossClassification.SUSPECTED_LOSS)
                .confidence("HIGH")
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .build();

        List<ConservationOpportunity> opportunities = conservationEngine.identifyOpportunities(
                List.of(loss),
                List.of(),
                List.of()
        );

        assertThat(opportunities).hasSize(1);
        ConservationOpportunity opp = opportunities.get(0);
        assertThat(opp.getCategory()).isEqualTo("CONVEYANCE_LOSS");
        // 55% of 20000 = 11000
        assertThat(opp.getEstimatedPotentialSavingsM3()).isEqualByComparingTo(new BigDecimal("11000.00"));
        assertThat(opp.getTargetWorkflowUri()).isEqualTo("/network");
    }

    @Test
    @DisplayName("Synthesizes meter recalibration opportunity with null numerical savings")
    void synthesizesMeterRecalibrationWithoutFabricatedSavings() {
        var reconciliation = WaterReconciliationItem.builder()
                .id(UUID.randomUUID())
                .boundaryType("INTAKE")
                .boundaryName("District River Gate")
                .discrepancyPercent(new BigDecimal("18.50"))
                .discrepancyVolumeM3(new BigDecimal("45000.00"))
                .status(ReconciliationStatus.DISCREPANCY)
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .build();

        List<ConservationOpportunity> opportunities = conservationEngine.identifyOpportunities(
                List.of(),
                List.of(),
                List.of(reconciliation)
        );

        assertThat(opportunities).hasSize(1);
        ConservationOpportunity opp = opportunities.get(0);
        assertThat(opp.getCategory()).isEqualTo("METER_CALIBRATION");
        assertThat(opp.getEstimatedPotentialSavingsM3()).isNull(); // Non-fabrication guarantee: uncalculable
        assertThat(opp.getEstimationMethodology()).containsIgnoringCase("Not calculable");
    }
}
