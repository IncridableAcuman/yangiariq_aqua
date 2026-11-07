package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DistributionEquityResponse(
        UUID clusterId,
        String clusterName,
        String clusterType,
        int sampleCount,
        BigDecimal meanDeliveryPerHectareM3,
        BigDecimal varianceM3,
        BigDecimal coefficientOfVariation,
        BigDecimal meanAllocationFulfillmentPercent,
        BigDecimal averageDeliveryDelayHours,
        String disparityLevel,
        String fairnessPolicyApplied,
        boolean isConsistentWithPolicy,
        String evidenceClassification,
        List<String> supportingEvidence,
        String notes
) {}
