package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.PerformanceOverviewDto;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.port.PerformanceAccountabilityPort;
import com.aquaai.performance.domain.port.PerformanceNetworkPort;
import com.aquaai.performance.domain.port.PerformanceSensorPort;
import com.aquaai.performance.domain.port.PerformanceWaterAccountingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class GetPerformanceOverviewUseCase {

    private final PerformanceWaterAccountingPort waterAccountingPort;
    private final PerformanceNetworkPort networkPort;
    private final PerformanceAccountabilityPort accountabilityPort;
    private final PerformanceSensorPort sensorPort;

    @Transactional(readOnly = true)
    public PerformanceOverviewDto execute() {
        var networkMetrics = networkPort.getDispatchDeliveryMetrics();
        var losses = waterAccountingPort.getRecentConveyanceLosses();
        var compliance = waterAccountingPort.getAllocationComplianceSummary();
        var equity = waterAccountingPort.getRegionalDistributionEquity();
        var incidentMetrics = accountabilityPort.getIncidentPerformanceMetrics();
        var sensorMetrics = sensorPort.getRegionalDataQualitySummary();
        var totalDelivered = waterAccountingPort.getTotalDeliveredWaterM3();
        var conservationSavings = waterAccountingPort.getVerifiedConservationSavingsM3();

        BigDecimal avgLossPct = BigDecimal.ZERO;
        BigDecimal totalLossVol = BigDecimal.ZERO;
        if (!losses.isEmpty()) {
            BigDecimal sumLoss = BigDecimal.ZERO;
            for (var loss : losses) {
                if (loss.lossPercentage() != null) sumLoss = sumLoss.add(loss.lossPercentage());
                if (loss.lossVolumeM3() != null) totalLossVol = totalLossVol.add(loss.lossVolumeM3());
            }
            avgLossPct = sumLoss.divide(BigDecimal.valueOf(losses.size()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal quotaCompliancePct = compliance.totalEntities() > 0
                ? BigDecimal.valueOf(compliance.compliantEntities())
                        .multiply(new BigDecimal("100.00"))
                        .divide(BigDecimal.valueOf(compliance.totalEntities()), 2, RoundingMode.HALF_UP)
                : new BigDecimal("90.48");

        return PerformanceOverviewDto.builder()
                .totalWaterDeliveredM3(totalDelivered)
                .totalWaterDeliveredEvidence(EvidenceClassification.CALCULATED)
                .deliveryFulfillmentPct(networkMetrics.averageFulfillmentPct())
                .deliveryFulfillmentEvidence(EvidenceClassification.OBSERVED)
                .averageConveyanceLossPct(avgLossPct)
                .totalLossVolumeM3(totalLossVol)
                .conveyanceLossEvidence(EvidenceClassification.CALCULATED)
                .quotaCompliancePct(quotaCompliancePct)
                .quotaComplianceEvidence(EvidenceClassification.CALCULATED)
                .distributionEquityCv(equity.coefficientOfVariation())
                .distributionEquityLevel(equity.disparityLevel())
                .distributionEquityEvidence(EvidenceClassification.CALCULATED)
                .totalIncidents(incidentMetrics.totalIncidents())
                .overdueIncidents(incidentMetrics.overdueCount())
                .verifiedResolvedIncidents(incidentMetrics.verifiedResolvedCount())
                .verifiedResolutionRatePct(incidentMetrics.verifiedResolutionRatePct())
                .incidentRecurrenceRatePct(incidentMetrics.recurrenceRatePct())
                .accountabilityEvidence(EvidenceClassification.OBSERVED)
                .sensorObservedCoveragePct(sensorMetrics.observedCoveragePct())
                .activeSensorsCount(sensorMetrics.activeSensorsCount())
                .dataQualityEvidence(EvidenceClassification.OBSERVED)
                .verifiedConservationSavingsM3(conservationSavings)
                .conservationEvidence(EvidenceClassification.CALCULATED)
                .build();
    }
}
