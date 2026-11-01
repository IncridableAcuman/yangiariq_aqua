package com.aquaai.irrigation.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object encapsulating volumetric and physical depth requirements for parcel irrigation.
 */
@Getter
@Builder
public class IrrigationRequirement {

    private final BigDecimal deficitDepthMm;
    private final BigDecimal netRequirementMm;
    private final BigDecimal grossVolumeM3;
    private final int estimatedDurationMinutes;
    private final BigDecimal flowRateM3H;
    private final BigDecimal irrigationEfficiency;

    public IrrigationRequirement(
            BigDecimal deficitDepthMm,
            BigDecimal netRequirementMm,
            BigDecimal grossVolumeM3,
            int estimatedDurationMinutes,
            BigDecimal flowRateM3H,
            BigDecimal irrigationEfficiency
    ) {
        this.deficitDepthMm = deficitDepthMm != null ? deficitDepthMm.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.netRequirementMm = netRequirementMm != null ? netRequirementMm.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.grossVolumeM3 = grossVolumeM3 != null ? grossVolumeM3.setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.estimatedDurationMinutes = Math.max(0, estimatedDurationMinutes);
        this.flowRateM3H = flowRateM3H != null ? flowRateM3H.setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(50.0);
        this.irrigationEfficiency = irrigationEfficiency != null ? irrigationEfficiency.setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(0.75);
    }
}
