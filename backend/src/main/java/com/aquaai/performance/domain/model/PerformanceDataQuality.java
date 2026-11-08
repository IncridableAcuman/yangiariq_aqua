package com.aquaai.performance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Audit record of data lineage and empirical coverage behind a performance calculation.
 */
@Getter
@Builder
public class PerformanceDataQuality {
    private final long totalReadingCount;
    private final long observedReadingCount;
    private final long simulatedReadingCount;
    private final long estimatedReadingCount;
    private final long missingIntervalCount;
    private final BigDecimal observedCoveragePercent;
    private final EvidenceClassification overallClassification;
    private final boolean meetsQualityThreshold;

    public static PerformanceDataQuality evaluate(
            long total,
            long observed,
            long simulated,
            long estimated,
            long missing,
            BigDecimal minimumQualityThresholdPct
    ) {
        BigDecimal coverage;
        if (total > 0) {
            coverage = BigDecimal.valueOf(observed)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        } else {
            coverage = BigDecimal.ZERO;
        }

        EvidenceClassification classification;
        if (total == 0 || missing == total) {
            classification = EvidenceClassification.MISSING;
        } else if (observed > 0 && simulated == 0 && estimated == 0) {
            classification = EvidenceClassification.OBSERVED;
        } else if (simulated > observed) {
            classification = EvidenceClassification.SIMULATED;
        } else if (estimated > 0) {
            classification = EvidenceClassification.ESTIMATED;
        } else {
            classification = EvidenceClassification.CALCULATED;
        }

        BigDecimal minThreshold = minimumQualityThresholdPct != null ? minimumQualityThresholdPct : new BigDecimal("80.00");
        boolean meets = coverage.compareTo(minThreshold) >= 0 && classification != EvidenceClassification.SIMULATED;

        return PerformanceDataQuality.builder()
                .totalReadingCount(total)
                .observedReadingCount(observed)
                .simulatedReadingCount(simulated)
                .estimatedReadingCount(estimated)
                .missingIntervalCount(missing)
                .observedCoveragePercent(coverage)
                .overallClassification(classification)
                .meetsQualityThreshold(meets)
                .build();
    }
}
