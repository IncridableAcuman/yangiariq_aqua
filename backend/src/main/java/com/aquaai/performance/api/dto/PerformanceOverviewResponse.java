package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.EvidenceClassification;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PerformanceOverviewResponse {
    private final BigDecimal totalWaterDeliveredM3;
    private final EvidenceClassification totalWaterDeliveredEvidence;

    private final BigDecimal deliveryFulfillmentPct;
    private final EvidenceClassification deliveryFulfillmentEvidence;

    private final BigDecimal averageConveyanceLossPct;
    private final BigDecimal totalLossVolumeM3;
    private final EvidenceClassification conveyanceLossEvidence;

    private final BigDecimal quotaCompliancePct;
    private final EvidenceClassification quotaComplianceEvidence;

    private final BigDecimal distributionEquityCv;
    private final String distributionEquityLevel;
    private final EvidenceClassification distributionEquityEvidence;

    private final long totalIncidents;
    private final long overdueIncidents;
    private final long verifiedResolvedIncidents;
    private final BigDecimal verifiedResolutionRatePct;
    private final BigDecimal incidentRecurrenceRatePct;
    private final EvidenceClassification accountabilityEvidence;

    private final BigDecimal sensorObservedCoveragePct;
    private final long activeSensorsCount;
    private final EvidenceClassification dataQualityEvidence;

    private final BigDecimal verifiedConservationSavingsM3;
    private final EvidenceClassification conservationEvidence;
}
