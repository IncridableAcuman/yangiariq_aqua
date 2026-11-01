package com.aquaai.intelligence.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * An individual identified agronomic signal supporting an assessment.
 */
@Getter
public class AgronomicSignal {

    private final UUID id;
    private final AgronomicSignalType signalType;
    private final IntelligenceSeverity severity;
    private final String metric;
    private final BigDecimal observedValue;
    private final BigDecimal thresholdValue;
    private final String message;

    public AgronomicSignal(
            UUID id,
            AgronomicSignalType signalType,
            IntelligenceSeverity severity,
            String metric,
            BigDecimal observedValue,
            BigDecimal thresholdValue,
            String message
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.signalType = Objects.requireNonNull(signalType, "signalType must not be null");
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.metric = Objects.requireNonNull(metric, "metric must not be null");
        this.observedValue = observedValue != null ? observedValue.setScale(4, RoundingMode.HALF_UP) : null;
        this.thresholdValue = thresholdValue != null ? thresholdValue.setScale(4, RoundingMode.HALF_UP) : null;
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    public static AgronomicSignal of(
            AgronomicSignalType signalType,
            IntelligenceSeverity severity,
            String metric,
            BigDecimal observedValue,
            BigDecimal thresholdValue,
            String message
    ) {
        return new AgronomicSignal(UUID.randomUUID(), signalType, severity, metric, observedValue, thresholdValue, message);
    }
}
