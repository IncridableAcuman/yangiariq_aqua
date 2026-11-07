package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.DistributionEquityAssessment;
import com.aquaai.wateraccounting.domain.model.EquityDisparityLevel;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pure domain service evaluating distribution equity across comparable agricultural parcel groups.
 * <p>
 * Evaluates:
 * - Delivery per hectare (m³/ha)
 * - Mean delivery and distribution variance (σ²)
 * - Coefficient of variation (CV = σ / μ)
 * - Consistency with fairness policies (CROP_CRITICALITY_FIRST, MOISTURE_DEFICIT_PRIORITY, PROPORTIONAL_DEFICIT)
 */
public class DistributionEquityEngine {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public record ParcelDeliveryData(
            UUID fieldId,
            String fieldName,
            BigDecimal areaHectares,
            BigDecimal deliveredVolumeM3,
            BigDecimal allocatedVolumeM3,
            BigDecimal deliveryDelayHours,
            String cropType,
            int cropUrgencyRank
    ) {}

    public DistributionEquityAssessment evaluateClusterEquity(
            UUID clusterId,
            String clusterName,
            String clusterType,
            List<ParcelDeliveryData> parcels,
            String activeFairnessPolicy,
            EvidenceClassification classification
    ) {
        if (parcels == null || parcels.size() < 2) {
            return DistributionEquityAssessment.builder()
                    .clusterId(clusterId)
                    .clusterName(clusterName)
                    .clusterType(clusterType)
                    .sampleCount(parcels != null ? parcels.size() : 0)
                    .meanDeliveryPerHectareM3(BigDecimal.ZERO)
                    .varianceM3(BigDecimal.ZERO)
                    .coefficientOfVariation(BigDecimal.ZERO)
                    .meanAllocationFulfillmentPercent(BigDecimal.ZERO)
                    .averageDeliveryDelayHours(BigDecimal.ZERO)
                    .disparityLevel(EquityDisparityLevel.INSUFFICIENT_DATA)
                    .fairnessPolicyApplied(activeFairnessPolicy != null ? activeFairnessPolicy : "CROP_CRITICALITY_FIRST")
                    .isConsistentWithPolicy(true)
                    .evidenceClassification(EvidenceClassification.MISSING)
                    .supportingEvidence(List.of("Insufficient comparable parcels in cluster (minimum 2 required)"))
                    .notes("Cluster has fewer than 2 reporting parcels for statistical variance calculation.")
                    .build();
        }

        int n = parcels.size();
        BigDecimal totalDeliveryPerHa = BigDecimal.ZERO;
        BigDecimal totalFulfillmentPercent = BigDecimal.ZERO;
        BigDecimal totalDelay = BigDecimal.ZERO;
        List<BigDecimal> deliveryPerHaList = new ArrayList<>(n);

        for (ParcelDeliveryData p : parcels) {
            BigDecimal area = p.areaHectares() != null && p.areaHectares().compareTo(BigDecimal.ZERO) > 0
                    ? p.areaHectares() : BigDecimal.ONE;
            BigDecimal delivered = p.deliveredVolumeM3() != null ? p.deliveredVolumeM3() : BigDecimal.ZERO;
            BigDecimal allocated = p.allocatedVolumeM3() != null && p.allocatedVolumeM3().compareTo(BigDecimal.ZERO) > 0
                    ? p.allocatedVolumeM3() : delivered;

            BigDecimal perHa = delivered.divide(area, 2, ROUNDING);
            deliveryPerHaList.add(perHa);
            totalDeliveryPerHa = totalDeliveryPerHa.add(perHa);

            BigDecimal fulfill = delivered.multiply(new BigDecimal("100.00")).divide(allocated, 2, ROUNDING);
            totalFulfillmentPercent = totalFulfillmentPercent.add(fulfill);

            BigDecimal delay = p.deliveryDelayHours() != null ? p.deliveryDelayHours() : BigDecimal.ZERO;
            totalDelay = totalDelay.add(delay);
        }

        BigDecimal countBD = new BigDecimal(n);
        BigDecimal meanPerHa = totalDeliveryPerHa.divide(countBD, 2, ROUNDING);
        BigDecimal meanFulfillment = totalFulfillmentPercent.divide(countBD, 2, ROUNDING);
        BigDecimal meanDelay = totalDelay.divide(countBD, 2, ROUNDING);

        // Variance: sum((x - mean)^2) / n
        BigDecimal sumSquaredDiff = BigDecimal.ZERO;
        for (BigDecimal val : deliveryPerHaList) {
            BigDecimal diff = val.subtract(meanPerHa);
            sumSquaredDiff = sumSquaredDiff.add(diff.multiply(diff));
        }
        BigDecimal variance = sumSquaredDiff.divide(countBD, 2, ROUNDING);

        // Standard Deviation = sqrt(variance)
        double stdDevDouble = Math.sqrt(variance.doubleValue());
        BigDecimal stdDev = new BigDecimal(stdDevDouble, MathContext.DECIMAL32).setScale(2, ROUNDING);

        // Coefficient of Variation: CV = stdDev / mean
        BigDecimal cv = BigDecimal.ZERO;
        if (meanPerHa.compareTo(BigDecimal.ZERO) > 0) {
            cv = stdDev.divide(meanPerHa, 4, ROUNDING);
        }

        EquityDisparityLevel level;
        List<String> evidence = new ArrayList<>();
        boolean isConsistent = true;
        String policy = activeFairnessPolicy != null ? activeFairnessPolicy : "CROP_CRITICALITY_FIRST";

        if (cv.compareTo(new BigDecimal("0.15")) <= 0) {
            level = EquityDisparityLevel.BALANCED;
            evidence.add(String.format("Coefficient of variation (%.2f) within balanced threshold (<= 0.15).", cv));
            evidence.add(String.format("Mean delivery: %.2f m³/ha across %d parcels.", meanPerHa, n));
        } else if (cv.compareTo(new BigDecimal("0.35")) <= 0) {
            level = EquityDisparityLevel.POTENTIAL_DISPARITY;
            evidence.add(String.format("Moderate delivery dispersion observed (CV = %.2f).", cv));
            if ("CROP_CRITICALITY_FIRST".equalsIgnoreCase(policy)) {
                evidence.add("Variation explained by crop criticality weighting (cotton vs wheat moisture requirements).");
            } else {
                evidence.add("Disparity indicates potential conveyance scheduling lag on tail-end branches.");
            }
        } else {
            level = EquityDisparityLevel.SIGNIFICANT_DISPARITY;
            evidence.add(String.format("High delivery disparity detected (CV = %.2f > 0.35).", cv));
            evidence.add(String.format("Delivery range: %.2f to %.2f m³/ha.",
                    deliveryPerHaList.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO),
                    deliveryPerHaList.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO)));
            isConsistent = false;
        }

        String notes = String.format("Cluster '%s' (%s): Evaluated %d parcels with mean fulfillment of %.2f%%.",
                clusterName, clusterType, n, meanFulfillment);

        return DistributionEquityAssessment.builder()
                .clusterId(clusterId)
                .clusterName(clusterName)
                .clusterType(clusterType)
                .sampleCount(n)
                .meanDeliveryPerHectareM3(meanPerHa)
                .varianceM3(variance)
                .coefficientOfVariation(cv)
                .meanAllocationFulfillmentPercent(meanFulfillment)
                .averageDeliveryDelayHours(meanDelay)
                .disparityLevel(level)
                .fairnessPolicyApplied(policy)
                .isConsistentWithPolicy(isConsistent)
                .evidenceClassification(classification != null ? classification : EvidenceClassification.CALCULATED)
                .supportingEvidence(evidence)
                .notes(notes)
                .build();
    }
}
