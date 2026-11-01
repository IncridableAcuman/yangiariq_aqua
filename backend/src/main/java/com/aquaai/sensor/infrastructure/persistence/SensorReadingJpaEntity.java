package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing the "sensor_readings" table.
 * Immutable telemetry observation record.
 */
@Getter
@Setter
@Entity
@Table(name = "sensor_readings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorReadingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sensor_id", nullable = false, updatable = false)
    private UUID sensorId;

    @Column(name = "field_id", nullable = false, updatable = false)
    private UUID fieldId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32, updatable = false)
    private SensorType type;

    @Column(name = "\"value\"", nullable = false, precision = 14, scale = 4, updatable = false)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 32, updatable = false)
    private MeasurementUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality", nullable = false, length = 16, updatable = false)
    @Builder.Default
    private ReadingQuality quality = ReadingQuality.VALID;

    @Column(name = "source", nullable = false, length = 64, updatable = false)
    private String source;

    @Column(name = "measured_at", nullable = false, updatable = false)
    private Instant measuredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
