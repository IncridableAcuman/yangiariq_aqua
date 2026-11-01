package com.aquaai.anomaly.infrastructure.persistence;

import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.sensor.domain.model.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anomaly_rule_configs")
@Getter
@Setter
@NoArgsConstructor
public class AnomalyRuleConfigJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 64)
    private String ruleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 32)
    private SensorType sensorType;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    private AnomalySeverity severity;

    @Column(name = "threshold", precision = 14, scale = 4, nullable = false)
    private BigDecimal threshold;

    @Column(name = "secondary_threshold", precision = 14, scale = 4)
    private BigDecimal secondaryThreshold;

    @Column(name = "time_window_minutes", nullable = false)
    private int timeWindowMinutes;

    @Column(name = "min_consecutive_readings", nullable = false)
    private int minConsecutiveReadings;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
