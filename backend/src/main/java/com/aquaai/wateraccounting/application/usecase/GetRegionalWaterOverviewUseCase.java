package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.wateraccounting.api.dto.RegionalWaterOverviewResponse;
import com.aquaai.wateraccounting.domain.model.AllocationComplianceStatus;
import com.aquaai.wateraccounting.domain.model.ReconciliationStatus;
import com.aquaai.wateraccounting.domain.repository.WaterLedgerRepository;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetRegionalWaterOverviewUseCase {

    private final GetRegionalWaterBalanceUseCase getBalanceUseCase;
    private final GetConveyanceLossAnalysisUseCase getLossesUseCase;
    private final GetAllocationComplianceUseCase getComplianceUseCase;
    private final GetDistributionEquityUseCase getEquityUseCase;
    private final GetConservationOpportunitiesUseCase getOpportunitiesUseCase;
    private final WaterLedgerRepository ledgerRepository;
    private final WaterReconciliationRepository reconciliationRepository;

    @Transactional(readOnly = true)
    public RegionalWaterOverviewResponse execute(Instant periodStart, Instant periodEnd) {
        var balance = getBalanceUseCase.execute("DISTRICT", null, "SEASONAL", periodStart, periodEnd);
        int totalLedger = (int) ledgerRepository.count();
        var allReconciliations = reconciliationRepository.findAll();
        int totalReconciliations = allReconciliations.size();
        int unverifiedReconciliations = (int) allReconciliations.stream()
                .filter(r -> r.getStatus() == ReconciliationStatus.DISCREPANCY || r.getStatus() == ReconciliationStatus.UNVERIFIED)
                .count();

        var losses = getLossesUseCase.execute();
        BigDecimal avgLoss = BigDecimal.ZERO;
        if (!losses.isEmpty()) {
            BigDecimal sumLoss = losses.stream()
                    .map(l -> l.lossPercentage())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgLoss = sumLoss.divide(new BigDecimal(losses.size()), 2, RoundingMode.HALF_UP);
        }

        var compliances = getComplianceUseCase.execute(null);
        int nonCompliant = (int) compliances.stream()
                .filter(c -> AllocationComplianceStatus.ALLOCATION_EXCEEDED.name().equals(c.status()) ||
                        AllocationComplianceStatus.APPROACHING_LIMIT.name().equals(c.status()) ||
                        AllocationComplianceStatus.UNDER_DELIVERED.name().equals(c.status()))
                .count();

        var opportunities = getOpportunitiesUseCase.execute();
        int activeOpportunities = opportunities.size();

        var equities = getEquityUseCase.execute();
        String equitySummary = "BALANCED";
        boolean hasSignificant = equities.stream().anyMatch(e -> "SIGNIFICANT_DISPARITY".equalsIgnoreCase(e.disparityLevel()));
        boolean hasPotential = equities.stream().anyMatch(e -> "POTENTIAL_DISPARITY".equalsIgnoreCase(e.disparityLevel()));
        if (hasSignificant) {
            equitySummary = "SIGNIFICANT_DISPARITY";
        } else if (hasPotential) {
            equitySummary = "POTENTIAL_DISPARITY";
        }

        return new RegionalWaterOverviewResponse(
                balance,
                totalLedger,
                totalReconciliations,
                unverifiedReconciliations,
                avgLoss,
                nonCompliant,
                activeOpportunities,
                equitySummary
        );
    }
}
