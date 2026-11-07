package com.aquaai.network.domain.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Policy governing water allocation equity during periods of canal contention or source deficit.
 */
@Builder
public record FairnessPolicy(
        String policyType, // PROPORTIONAL_DEFICIT, CROP_CRITICALITY_FIRST, MOISTURE_DEFICIT_PRIORITY, ROTATIONAL_EQUITY
        String description,
        BigDecimal minGuaranteedRatioPercent // e.g. 50.00% minimum guarantee
) {
    public static FairnessPolicy defaultPolicy() {
        return new FairnessPolicy(
                "CROP_CRITICALITY_FIRST",
                "Prioritize sensitive crops approaching wilting point, then apply proportional quota sharing.",
                new BigDecimal("40.00")
        );
    }
}
