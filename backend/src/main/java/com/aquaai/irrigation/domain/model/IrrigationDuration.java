package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object representing planned or actual duration of irrigation activity.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class IrrigationDuration implements Comparable<IrrigationDuration> {

    public static final IrrigationDuration ZERO = new IrrigationDuration(0);

    private final int minutes;

    private IrrigationDuration(int minutes) {
        if (minutes < 0) {
            throw new BusinessRuleException("Irrigation duration cannot be negative");
        }
        this.minutes = minutes;
    }

    public static IrrigationDuration ofMinutes(int minutes) {
        return new IrrigationDuration(minutes);
    }

    public static IrrigationDuration ofHours(double hours) {
        if (hours < 0) {
            throw new BusinessRuleException("Hours cannot be negative");
        }
        int totalMinutes = (int) Math.round(hours * 60.0);
        return new IrrigationDuration(totalMinutes);
    }

    public BigDecimal toHoursBigDecimal() {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    @Override
    public int compareTo(IrrigationDuration o) {
        return Integer.compare(this.minutes, o.minutes);
    }
}
