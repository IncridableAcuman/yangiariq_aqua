package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object representing delivery flow rate in m³/hour or L/s.
 * 1 m³/h = 1000 L / 3600 s = 0.27778 L/s.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class FlowRate implements Comparable<FlowRate> {

    public static final FlowRate ZERO = new FlowRate(BigDecimal.ZERO);
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);
    private static final BigDecimal LITERS_PER_M3 = BigDecimal.valueOf(1000);

    private final BigDecimal cubicMetersPerHour;

    private FlowRate(BigDecimal cubicMetersPerHour) {
        if (cubicMetersPerHour == null) {
            throw new BusinessRuleException("Flow rate cannot be null");
        }
        if (cubicMetersPerHour.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Flow rate cannot be negative");
        }
        this.cubicMetersPerHour = cubicMetersPerHour.setScale(2, RoundingMode.HALF_UP);
    }

    public static FlowRate ofM3PerHour(BigDecimal m3PerHour) {
        return new FlowRate(m3PerHour);
    }

    public static FlowRate ofM3PerHour(double m3PerHour) {
        return new FlowRate(BigDecimal.valueOf(m3PerHour));
    }

    public static FlowRate ofLitersPerSecond(BigDecimal litersPerSecond) {
        if (litersPerSecond == null) {
            throw new BusinessRuleException("Liters per second cannot be null");
        }
        // m3/h = (L/s * 3600) / 1000 = L/s * 3.6
        BigDecimal m3PerHour = litersPerSecond.multiply(BigDecimal.valueOf(3.6));
        return new FlowRate(m3PerHour);
    }

    public BigDecimal toLitersPerSecond() {
        return cubicMetersPerHour.divide(BigDecimal.valueOf(3.6), 2, RoundingMode.HALF_UP);
    }

    @Override
    public int compareTo(FlowRate o) {
        return this.cubicMetersPerHour.compareTo(o.cubicMetersPerHour);
    }
}
