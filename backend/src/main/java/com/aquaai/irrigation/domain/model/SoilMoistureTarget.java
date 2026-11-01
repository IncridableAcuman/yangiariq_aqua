package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object representing volumetric soil moisture targets and deficits (%).
 */
@Getter
@EqualsAndHashCode
@ToString
public final class SoilMoistureTarget {

    private final BigDecimal targetPercent;
    private final BigDecimal currentPercent;
    private final BigDecimal deficitPercent;

    public SoilMoistureTarget(BigDecimal targetPercent, BigDecimal currentPercent) {
        if (targetPercent == null || targetPercent.compareTo(BigDecimal.ZERO) < 0 || targetPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessRuleException("Target soil moisture must be between 0% and 100%");
        }
        if (currentPercent != null && (currentPercent.compareTo(BigDecimal.ZERO) < 0 || currentPercent.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BusinessRuleException("Current soil moisture must be between 0% and 100%");
        }

        this.targetPercent = targetPercent.setScale(2, RoundingMode.HALF_UP);
        this.currentPercent = currentPercent != null ? currentPercent.setScale(2, RoundingMode.HALF_UP) : null;

        if (this.currentPercent != null) {
            BigDecimal diff = this.targetPercent.subtract(this.currentPercent);
            this.deficitPercent = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
        } else {
            this.deficitPercent = BigDecimal.ZERO;
        }
    }

    public boolean hasDeficit() {
        return this.deficitPercent != null && this.deficitPercent.compareTo(BigDecimal.ZERO) > 0;
    }
}
