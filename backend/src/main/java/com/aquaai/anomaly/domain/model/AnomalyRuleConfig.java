package com.aquaai.anomaly.domain.model;

import com.aquaai.sensor.domain.model.SensorType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Configurable detection threshold and policy definition.
 * Governs deterministic and statistical anomaly rules without hardcoding agronomic assumptions.
 */
@Getter
public class AnomalyRuleConfig {

    private final UUID id;
    private final String ruleCode;
    private final SensorType sensorType;
    private String name;
    private String description;
    private boolean enabled;
    private AnomalySeverity severity;
    private BigDecimal threshold;
    private BigDecimal secondaryThreshold;
    private int timeWindowMinutes;
    private int minConsecutiveReadings;
    private final Instant createdAt;
    private Instant updatedAt;

    public AnomalyRuleConfig(
            UUID id,
            String ruleCode,
            SensorType sensorType,
            String name,
            String description,
            boolean enabled,
            AnomalySeverity severity,
            BigDecimal threshold,
            BigDecimal secondaryThreshold,
            int timeWindowMinutes,
            int minConsecutiveReadings,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.ruleCode = Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        this.sensorType = Objects.requireNonNull(sensorType, "sensorType must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.enabled = enabled;
        this.severity = severity != null ? severity : AnomalySeverity.MEDIUM;
        this.threshold = Objects.requireNonNull(threshold, "threshold must not be null");
        this.secondaryThreshold = secondaryThreshold;
        this.timeWindowMinutes = timeWindowMinutes > 0 ? timeWindowMinutes : 60;
        this.minConsecutiveReadings = minConsecutiveReadings > 0 ? minConsecutiveReadings : 1;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static AnomalyRuleConfig create(
            String ruleCode,
            SensorType sensorType,
            String name,
            String description,
            AnomalySeverity severity,
            BigDecimal threshold,
            BigDecimal secondaryThreshold,
            int timeWindowMinutes,
            int minConsecutiveReadings
    ) {
        Instant now = Instant.now();
        return new AnomalyRuleConfig(
                UUID.randomUUID(),
                ruleCode,
                sensorType,
                name,
                description,
                true,
                severity,
                threshold,
                secondaryThreshold,
                timeWindowMinutes,
                minConsecutiveReadings,
                now,
                now
        );
    }

    public void update(
            String name,
            String description,
            AnomalySeverity severity,
            BigDecimal threshold,
            BigDecimal secondaryThreshold,
            int timeWindowMinutes,
            int minConsecutiveReadings
    ) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        if (severity != null) {
            this.severity = severity;
        }
        if (threshold != null) {
            this.threshold = threshold;
        }
        this.secondaryThreshold = secondaryThreshold;
        if (timeWindowMinutes > 0) {
            this.timeWindowMinutes = timeWindowMinutes;
        }
        if (minConsecutiveReadings > 0) {
            this.minConsecutiveReadings = minConsecutiveReadings;
        }
        this.updatedAt = Instant.now();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }
}
