package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing volumetric water quantities, standardizing m³ and Liters.
 * 1 m³ = 1,000 Liters.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class WaterVolume implements Comparable<WaterVolume> {

    public static final WaterVolume ZERO = new WaterVolume(BigDecimal.ZERO);
    private static final BigDecimal LITERS_PER_M3 = BigDecimal.valueOf(1000);

    private final BigDecimal cubicMeters;

    private WaterVolume(BigDecimal cubicMeters) {
        if (cubicMeters == null) {
            throw new BusinessRuleException("Water volume cannot be null");
        }
        if (cubicMeters.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Water volume cannot be negative");
        }
        this.cubicMeters = cubicMeters.setScale(4, RoundingMode.HALF_UP);
    }

    public static WaterVolume ofCubicMeters(BigDecimal cubicMeters) {
        return new WaterVolume(cubicMeters);
    }

    public static WaterVolume ofCubicMeters(double cubicMeters) {
        return new WaterVolume(BigDecimal.valueOf(cubicMeters));
    }

    public static WaterVolume ofLiters(BigDecimal liters) {
        if (liters == null) {
            throw new BusinessRuleException("Liters cannot be null");
        }
        return new WaterVolume(liters.divide(LITERS_PER_M3, 4, RoundingMode.HALF_UP));
    }

    public BigDecimal toLiters() {
        return cubicMeters.multiply(LITERS_PER_M3).setScale(2, RoundingMode.HALF_UP);
    }

    public WaterVolume add(WaterVolume other) {
        Objects.requireNonNull(other, "Other volume must not be null");
        return new WaterVolume(this.cubicMeters.add(other.cubicMeters));
    }

    public WaterVolume subtract(WaterVolume other) {
        Objects.requireNonNull(other, "Other volume must not be null");
        BigDecimal diff = this.cubicMeters.subtract(other.cubicMeters);
        if (diff.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        return new WaterVolume(diff);
    }

    public boolean isGreaterThan(WaterVolume other) {
        return compareTo(other) > 0;
    }

    public boolean isGreaterThanZero() {
        return this.cubicMeters.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public int compareTo(WaterVolume o) {
        return this.cubicMeters.compareTo(o.cubicMeters);
    }
}
