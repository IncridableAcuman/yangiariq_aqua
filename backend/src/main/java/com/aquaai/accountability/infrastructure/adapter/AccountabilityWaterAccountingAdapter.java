package com.aquaai.accountability.infrastructure.adapter;

import com.aquaai.accountability.domain.port.AccountabilityWaterAccountingPort;
import com.aquaai.wateraccounting.domain.model.ReconciliationStatus;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountabilityWaterAccountingAdapter implements AccountabilityWaterAccountingPort {

    private final WaterReconciliationRepository reconciliationRepository;

    @Override
    public List<DiscrepancyFinding> findPendingDiscrepancies() {
        return reconciliationRepository.findAll().stream()
                .filter(item -> item.getStatus() == ReconciliationStatus.DISCREPANCY)
                .map(this::mapDiscrepancy)
                .collect(Collectors.toList());
    }

    @Override
    public List<LossFinding> findSignificantConveyanceLosses() {
        // Look for items with high loss or flagged status in conveyance reconciliations
        List<LossFinding> findings = new ArrayList<>();
        List<WaterReconciliationItem> items = reconciliationRepository.findAll().stream()
                .filter(item -> item.getStatus() == ReconciliationStatus.DISCREPANCY)
                .collect(Collectors.toList());
        for (WaterReconciliationItem item : items) {
            if ("CANAL_REACH".equalsIgnoreCase(item.getBoundaryType())) {
                findings.add(new LossFinding(
                        item.getTargetId(),
                        item.getBoundaryName(),
                        item.getDiscrepancyVolumeM3(),
                        item.getDiscrepancyPercent(),
                        "SUSPECTED_LOSS",
                        item.getPeriodEnd() != null ? item.getPeriodEnd() : Instant.now()
                ));
            }
        }
        return findings;
    }

    private DiscrepancyFinding mapDiscrepancy(WaterReconciliationItem item) {
        return new DiscrepancyFinding(
                item.getId(),
                item.getBoundaryType(),
                item.getBoundaryName(),
                item.getTargetId(),
                item.getDiscrepancyVolumeM3(),
                item.getDiscrepancyPercent(),
                item.getExplanation(),
                item.getCreatedAt() != null ? item.getCreatedAt() : Instant.now()
        );
    }
}
