package com.aquaai.wateraccounting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Distribution equity evaluation comparing water delivery across comparable agricultural groups.
 */
@Getter
@Builder(toBuilder = true)
public class DistributionEquityAssessment {

    private final UUID clusterId;
    private final String clusterName;
    private final String clusterType;
    private final int sampleCount;
    private final BigDecimal meanDeliveryPerHectareM3;
    private final BigDecimal varianceM3;
    private final BigDecimal coefficientOfVariation;
    private final BigDecimal meanAllocationFulfillmentPercent;
    private final BigDecimal averageDeliveryDelayHours;
    private final EquityDisparityLevel disparityLevel;
    private final String fairnessPolicyApplied;
    private final boolean isConsistentWithPolicy;
    private final EvidenceClassification evidenceClassification;
    private final List<String> supportingEvidence;
    private final String notes;

    public DistributionEquityAssessment(
            UUID clusterId,
            String clusterName,
            String clusterType,
            int sampleCount,
            BigDecimal meanDeliveryPerHectareM3,
            BigDecimal varianceM3,
            BigDecimal coefficientOfVariation,
            BigDecimal meanAllocationFulfillmentPercent,
            BigDecimal averageDeliveryDelayHours,
            EquityDisparityLevel disparityLevel,
            String fairnessPolicyApplied,
            boolean isConsistentWithPolicy,
            EvidenceClassification evidenceClassification,
            List<String> supportingEvidence,
            String notes
    ) {
        this.clusterId = clusterId;
        this.clusterName = clusterName != null ? clusterName : "Cluster";
        this.clusterType = clusterType != null ? clusterType : "WUA";
        this.sampleCount = sampleCount;
        this.meanDeliveryPerHectareM3 = meanDeliveryPerHectareM3 != null ? meanDeliveryPerHectareM3 : BigDecimal.ZERO;
        this.varianceM3 = varianceM3 != null ? varianceM3 : BigDecimal.ZERO;
        this.coefficientOfVariation = coefficientOfVariation != null ? coefficientOfVariation : BigDecimal.ZERO;
        this.meanAllocationFulfillmentPercent = meanAllocationFulfillmentPercent != null ? meanAllocationFulfillmentPercent : BigDecimal.ZERO;
        this.averageDeliveryDelayHours = averageDeliveryDelayHours != null ? averageDeliveryDelayHours : BigDecimal.ZERO;
        this.disparityLevel = disparityLevel != null ? disparityLevel : EquityDisparityLevel.BALANCED;
        this.fairnessPolicyApplied = fairnessPolicyApplied != null ? fairnessPolicyApplied : "CROP_CRITICALITY_FIRST";
        this.isConsistentWithPolicy = isConsistentWithPolicy;
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.supportingEvidence = supportingEvidence != null ? supportingEvidence : List.of();
        this.notes = notes;
    }
}
