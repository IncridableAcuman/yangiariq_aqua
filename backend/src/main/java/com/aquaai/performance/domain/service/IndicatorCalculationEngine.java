package com.aquaai.performance.domain.service;

import com.aquaai.performance.domain.model.ChangeDirection;
import com.aquaai.performance.domain.model.EvidenceClassification;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure domain engine executing deterministic indicator mathematical calculations.
 * <p>
 * Guarantee: Zero division protection, non-fabrication of missing records, and explicit half-up rounding.
 */
public class IndicatorCalculationEngine {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calculates delivery fulfillment ratio (%): (actual / planned) * 100.
     * If planned volume is zero, returns ZERO if actual is zero, or 100.0 if not planned but delivered under special emergency quota.
     */
    public BigDecimal calculateDeliveryFulfillment(BigDecimal actualVolumeM3, BigDecimal plannedVolumeM3) {
        if (plannedVolumeM3 == null || plannedVolumeM3.compareTo(BigDecimal.ZERO) <= 0) {
            if (actualVolumeM3 != null && actualVolumeM3.compareTo(BigDecimal.ZERO) > 0) {
                return new BigDecimal("100.00");
            }
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }
        if (actualVolumeM3 == null || actualVolumeM3.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }

        return actualVolumeM3
                .multiply(new BigDecimal("100.00"))
                .divide(plannedVolumeM3, 2, ROUNDING);
    }

    /**
     * Calculates conveyance loss percentage (%): (lossVolume / upstreamVolume) * 100.
     */
    public BigDecimal calculateConveyanceLossPercentage(BigDecimal lossVolumeM3, BigDecimal upstreamVolumeM3) {
        if (upstreamVolumeM3 == null || upstreamVolumeM3.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }
        if (lossVolumeM3 == null || lossVolumeM3.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }

        return lossVolumeM3
                .multiply(new BigDecimal("100.00"))
                .divide(upstreamVolumeM3, 2, ROUNDING);
    }

    /**
     * Calculates quota utilization ratio (%): (delivered / allocatedQuota) * 100.
     */
    public BigDecimal calculateQuotaUtilization(BigDecimal deliveredVolumeM3, BigDecimal allocatedQuotaM3) {
        if (allocatedQuotaM3 == null || allocatedQuotaM3.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }
        if (deliveredVolumeM3 == null || deliveredVolumeM3.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }

        return deliveredVolumeM3
                .multiply(new BigDecimal("100.00"))
                .divide(allocatedQuotaM3, 2, ROUNDING);
    }

    /**
     * Calculates schedule adherence (%): (completedIrrigations / plannedIrrigations) * 100.
     */
    public BigDecimal calculateScheduleAdherence(long completedIrrigations, long plannedIrrigations) {
        if (plannedIrrigations <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }
        return BigDecimal.valueOf(completedIrrigations)
                .multiply(new BigDecimal("100.00"))
                .divide(BigDecimal.valueOf(plannedIrrigations), 2, ROUNDING);
    }

    /**
     * Calculates resolution rate (%): (verifiedResolved / totalIncidents) * 100.
     * Note: Only independently verified incidents count as resolved.
     */
    public BigDecimal calculateResolutionRate(long verifiedResolvedCount, long totalIncidentCount) {
        if (totalIncidentCount <= 0) {
            return new BigDecimal("100.00");
        }
        return BigDecimal.valueOf(verifiedResolvedCount)
                .multiply(new BigDecimal("100.00"))
                .divide(BigDecimal.valueOf(totalIncidentCount), 2, ROUNDING);
    }

    /**
     * Calculates incident recurrence rate (%): (recurringIncidents / totalIncidents) * 100.
     */
    public BigDecimal calculateRecurrenceRate(long recurringIncidentCount, long totalIncidentCount) {
        if (totalIncidentCount <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }
        return BigDecimal.valueOf(recurringIncidentCount)
                .multiply(new BigDecimal("100.00"))
                .divide(BigDecimal.valueOf(totalIncidentCount), 2, ROUNDING);
    }

    /**
     * Calculates observed data coverage (%): (observedReadings / totalReadings) * 100.
     */
    public BigDecimal calculateDataCoverage(long observedReadings, long totalReadings) {
        if (totalReadings <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }
        return BigDecimal.valueOf(observedReadings)
                .multiply(new BigDecimal("100.00"))
                .divide(BigDecimal.valueOf(totalReadings), 2, ROUNDING);
    }

    /**
     * Calculates absolute and percentage change between baseline and post-intervention value.
     */
    public BigDecimal calculatePercentageChange(BigDecimal postValue, BigDecimal baselineValue) {
        if (baselineValue == null || baselineValue.compareTo(BigDecimal.ZERO) == 0) {
            if (postValue != null && postValue.compareTo(BigDecimal.ZERO) != 0) {
                return new BigDecimal("100.00");
            }
            return BigDecimal.ZERO.setScale(2, ROUNDING);
        }
        if (postValue == null) {
            return new BigDecimal("-100.00");
        }

        BigDecimal diff = postValue.subtract(baselineValue);
        return diff.multiply(new BigDecimal("100.00"))
                .divide(baselineValue.abs(), 2, ROUNDING);
    }

    /**
     * Evaluates change direction based on whether higher or lower is favorable for the indicator.
     */
    public ChangeDirection evaluateChangeDirection(String indicatorCode, BigDecimal diff) {
        if (diff == null || diff.compareTo(BigDecimal.ZERO) == 0) {
            return ChangeDirection.NEUTRAL;
        }

        boolean higherIsBetter = isHigherValueFavorable(indicatorCode);
        if (higherIsBetter) {
            return diff.compareTo(BigDecimal.ZERO) > 0 ? ChangeDirection.FAVORABLE : ChangeDirection.UNFAVORABLE;
        } else {
            return diff.compareTo(BigDecimal.ZERO) < 0 ? ChangeDirection.FAVORABLE : ChangeDirection.UNFAVORABLE;
        }
    }

    private boolean isHigherValueFavorable(String indicatorCode) {
        if (indicatorCode == null) return true;
        String code = indicatorCode.toUpperCase();
        // Lower is favorable for losses, recurrence, overdue, disparity, missing data
        if (code.contains("LOSS") || code.contains("RECURRENCE") || code.contains("DISPARITY")
                || code.contains("OVERDUE") || code.contains("MISSING") || code.contains("DELAY")) {
            return false;
        }
        return true;
    }
}
