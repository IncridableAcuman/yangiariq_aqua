package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.ReconciliationStatus;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pure domain service for multi-boundary water accounting reconciliation.
 */
public class WaterReconciliationEngine {

    public static final BigDecimal DEFAULT_TOLERANCE = new BigDecimal("5.00");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public record BoundaryReconciliationInput(
            String boundaryType,
            String boundaryName,
            UUID targetId,
            Instant periodStart,
            Instant periodEnd,
            BigDecimal expectedVolumeM3,
            BigDecimal recordedVolumeM3,
            BigDecimal customTolerancePercent,
            EvidenceClassification classification
    ) {}

    public List<WaterReconciliationItem> reconcileBoundaries(List<BoundaryReconciliationInput> inputs) {
        List<WaterReconciliationItem> results = new ArrayList<>();
        if (inputs == null) return results;

        for (BoundaryReconciliationInput in : inputs) {
            BigDecimal expected = in.expectedVolumeM3() != null ? in.expectedVolumeM3() : BigDecimal.ZERO;
            BigDecimal recorded = in.recordedVolumeM3() != null ? in.recordedVolumeM3() : BigDecimal.ZERO;
            BigDecimal diff = recorded.subtract(expected);
            BigDecimal tol = in.customTolerancePercent() != null ? in.customTolerancePercent() : DEFAULT_TOLERANCE;

            BigDecimal diffPercent = BigDecimal.ZERO;
            if (expected.compareTo(BigDecimal.ZERO) > 0) {
                diffPercent = diff.abs().multiply(new BigDecimal("100.00")).divide(expected, 2, ROUNDING);
            }

            ReconciliationStatus status;
            String explanation;

            if (expected.compareTo(BigDecimal.ZERO) <= 0 && recorded.compareTo(BigDecimal.ZERO) <= 0) {
                status = ReconciliationStatus.INSUFFICIENT_DATA;
                explanation = "Zero volume recorded across both boundary stages.";
            } else if (diffPercent.compareTo(BigDecimal.ZERO) == 0) {
                status = ReconciliationStatus.RECONCILED;
                explanation = "Exact volume match between stages (zero variance).";
            } else if (diffPercent.compareTo(tol) <= 0) {
                status = ReconciliationStatus.WITHIN_TOLERANCE;
                explanation = String.format("Variance of %.2f m³ (%.2f%%) is within allowable %.2f%% hydrometric tolerance.", diff, diffPercent, tol);
            } else {
                status = ReconciliationStatus.DISCREPANCY;
                explanation = String.format("Significant discrepancy: %.2f m³ (%.2f%%) exceeds %.2f%% tolerance threshold.", diff, diffPercent, tol);
            }

            results.add(WaterReconciliationItem.builder()
                    .id(UUID.randomUUID())
                    .boundaryType(in.boundaryType())
                    .boundaryName(in.boundaryName())
                    .targetId(in.targetId())
                    .periodStart(in.periodStart())
                    .periodEnd(in.periodEnd())
                    .expectedVolumeM3(expected.setScale(2, ROUNDING))
                    .recordedVolumeM3(recorded.setScale(2, ROUNDING))
                    .discrepancyVolumeM3(diff.setScale(2, ROUNDING))
                    .discrepancyPercent(diffPercent)
                    .tolerancePercent(tol)
                    .status(status)
                    .evidenceClassification(in.classification() != null ? in.classification() : EvidenceClassification.CALCULATED)
                    .explanation(explanation)
                    .build());
        }

        return results;
    }
}
