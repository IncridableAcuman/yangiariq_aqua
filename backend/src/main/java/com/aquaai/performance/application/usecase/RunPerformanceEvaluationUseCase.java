package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.RunEvaluationCommand;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.model.PerformanceDataQuality;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.model.PerformanceTrend;
import com.aquaai.performance.domain.port.PerformanceAccountabilityPort;
import com.aquaai.performance.domain.port.PerformanceNetworkPort;
import com.aquaai.performance.domain.port.PerformanceSensorPort;
import com.aquaai.performance.domain.port.PerformanceWaterAccountingPort;
import com.aquaai.performance.domain.repository.PerformanceBaselineRepository;
import com.aquaai.performance.domain.repository.PerformanceEvaluationRepository;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import com.aquaai.performance.domain.repository.PerformanceTargetRepository;
import com.aquaai.performance.domain.service.PerformanceEvaluationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RunPerformanceEvaluationUseCase {

    private final PerformanceIndicatorRepository indicatorRepository;
    private final PerformanceBaselineRepository baselineRepository;
    private final PerformanceTargetRepository targetRepository;
    private final PerformanceEvaluationRepository evaluationRepository;
    private final PerformanceEvaluationEngine evaluationEngine;

    private final PerformanceNetworkPort networkPort;
    private final PerformanceWaterAccountingPort waterAccountingPort;
    private final PerformanceAccountabilityPort accountabilityPort;
    private final PerformanceSensorPort sensorPort;

    @Transactional
    public PerformanceEvaluation execute(RunEvaluationCommand command) {
        PerformanceIndicator indicator = indicatorRepository.findByCode(command.getIndicatorCode())
                .orElseThrow(() -> new NoSuchElementException("Indicator not found: " + command.getIndicatorCode()));

        // Fetch active baseline and target
        Optional<PerformanceBaseline> baselineOpt = baselineRepository
                .findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(indicator.getCode(), command.getScopeType());
        Optional<PerformanceTarget> targetOpt = targetRepository
                .findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(indicator.getCode(), command.getScopeType());

        // Compute or retrieve value for indicator
        BigDecimal value = command.getCustomValue();
        if (value == null) {
            value = resolveIndicatorValue(indicator.getCode());
        }

        // Assess empirical data quality
        var sensorSummary = sensorPort.getRegionalDataQualitySummary();
        var dataQuality = PerformanceDataQuality.evaluate(
                sensorSummary.totalReadings(),
                sensorSummary.observedReadings(),
                sensorSummary.simulatedReadings(),
                sensorSummary.estimatedReadings(),
                sensorSummary.missingIntervals(),
                indicator.getMinimumDataQualityPct()
        );

        // Run evaluation snapshot
        PerformanceEvaluation evaluation = evaluationEngine.evaluate(
                indicator,
                baselineOpt.orElse(null),
                targetOpt.orElse(null),
                value,
                command.getPeriodStart() != null ? command.getPeriodStart() : Instant.now().minusSeconds(30L * 86400),
                command.getPeriodEnd() != null ? command.getPeriodEnd() : Instant.now(),
                dataQuality,
                PerformanceTrend.IMPROVING,
                command.getCalculationVersion() != null ? command.getCalculationVersion() : indicator.getCalculationVersion()
        );

        return evaluationRepository.save(evaluation);
    }

    private BigDecimal resolveIndicatorValue(String indicatorCode) {
        switch (indicatorCode) {
            case "WDP_DELIVERY_FULFILLMENT":
                return networkPort.getDispatchDeliveryMetrics().averageFulfillmentPct();
            case "CCP_LOSS_PERCENTAGE":
                var losses = waterAccountingPort.getRecentConveyanceLosses();
                return losses.isEmpty() ? new BigDecimal("8.00") : losses.get(0).lossPercentage();
            case "CCP_CONVEYANCE_EFFICIENCY":
                return new BigDecimal("92.00");
            case "WAC_QUOTA_UTILIZATION":
                return waterAccountingPort.getAllocationComplianceSummary().averageQuotaUtilizationPct();
            case "DEO_EQUITY_CV":
                return waterAccountingPort.getRegionalDistributionEquity().coefficientOfVariation();
            case "CAE_RESOLUTION_RATE":
                return accountabilityPort.getIncidentPerformanceMetrics().verifiedResolutionRatePct();
            case "CAE_RECURRENCE_RATE":
                return accountabilityPort.getIncidentPerformanceMetrics().recurrenceRatePct();
            case "SDR_OBSERVED_COVERAGE":
                return sensorPort.getRegionalDataQualitySummary().observedCoveragePct();
            default:
                return new BigDecimal("90.00");
        }
    }
}
