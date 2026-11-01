package com.aquaai.sensor.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable domain record representing an observed telemetry measurement produced by a field sensor.
 */
@Getter
public class SensorReading {

    public static final int MAX_SOURCE_LENGTH = 64;
    public static final int VALUE_SCALE = 4;

    private final UUID id;
    private final UUID sensorId;
    private final UUID fieldId;
    private final SensorType type;
    private final BigDecimal value;
    private final MeasurementUnit unit;
    private final ReadingQuality quality;
    private final String source;
    private final Instant measuredAt;
    private final Instant createdAt;

    /**
     * Factory method for creating a brand-new telemetry reading.
     */
    public static SensorReading create(
            UUID sensorId,
            UUID fieldId,
            SensorType type,
            BigDecimal value,
            MeasurementUnit unit,
            ReadingQuality quality,
            String source,
            Instant measuredAt
    ) {
        return new SensorReading(
                UUID.randomUUID(),
                sensorId,
                fieldId,
                type,
                value,
                unit,
                quality != null ? quality : ReadingQuality.VALID,
                source != null ? source : "UNKNOWN",
                measuredAt != null ? measuredAt : Instant.now(),
                Instant.now()
        );
    }

    /**
     * Full reconstitution constructor (used by persistence mappers and factory).
     */
    public SensorReading(
            UUID id,
            UUID sensorId,
            UUID fieldId,
            SensorType type,
            BigDecimal value,
            MeasurementUnit unit,
            ReadingQuality quality,
            String source,
            Instant measuredAt,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Reading ID must not be null");
        this.sensorId = Objects.requireNonNull(sensorId, "Sensor ID must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.type = Objects.requireNonNull(type, "Sensor type must not be null");
        this.unit = Objects.requireNonNull(unit, "Measurement unit must not be null");
        this.quality = quality != null ? quality : ReadingQuality.VALID;
        this.measuredAt = Objects.requireNonNull(measuredAt, "Measured timestamp must not be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();

        // Enforce physical bounds & unit compatibility
        this.type.validateValue(value, this.unit);
        this.value = value.setScale(VALUE_SCALE, RoundingMode.HALF_UP);

        // Enforce clock sanity: reading timestamp cannot be in the future (allowing max 5 min clock skew)
        Instant maxFutureAllowed = Instant.now().plus(5, ChronoUnit.MINUTES);
        if (this.measuredAt.isAfter(maxFutureAllowed)) {
            throw new BusinessRuleException(
                    "Sensor reading timestamp cannot be in the future: " + this.measuredAt,
                    "FUTURE_MEASUREMENT_TIMESTAMP"
            );
        }

        // Validate source string
        if (source != null && source.length() > MAX_SOURCE_LENGTH) {
            this.source = source.substring(0, MAX_SOURCE_LENGTH);
        } else {
            this.source = (source != null && !source.isBlank()) ? source.trim() : "UNKNOWN";
        }
    }
}
