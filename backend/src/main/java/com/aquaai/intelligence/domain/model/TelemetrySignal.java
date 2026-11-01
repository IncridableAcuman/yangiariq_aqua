package com.aquaai.intelligence.domain.model;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Normalized telemetry signal representing a validated observation used by intelligence services.
 */
@Getter
public class TelemetrySignal {

    private final UUID fieldId;
    private final UUID sensorId;
    private final SensorType type;
    private final BigDecimal value;
    private final MeasurementUnit unit;
    private final ReadingQuality quality;
    private final Instant measuredAt;
    private final TelemetryFreshness freshness;

    public TelemetrySignal(
            UUID fieldId,
            UUID sensorId,
            SensorType type,
            BigDecimal value,
            MeasurementUnit unit,
            ReadingQuality quality,
            Instant measuredAt,
            TelemetryFreshness freshness
    ) {
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.sensorId = sensorId;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null").setScale(4, RoundingMode.HALF_UP);
        this.unit = Objects.requireNonNull(unit, "unit must not be null");
        this.quality = quality != null ? quality : ReadingQuality.VALID;
        this.measuredAt = Objects.requireNonNull(measuredAt, "measuredAt must not be null");
        this.freshness = freshness != null ? freshness : TelemetryFreshness.FRESH;

        if (this.quality == ReadingQuality.INVALID && this.freshness == TelemetryFreshness.FRESH) {
            // Normal behavior: INVALID quality remains acknowledged but flagged
        }
    }

    public boolean isAuthoritative() {
        return this.quality == ReadingQuality.VALID && this.freshness == TelemetryFreshness.FRESH;
    }

    public boolean isSuspectOrStale() {
        return this.quality == ReadingQuality.SUSPECT || this.freshness == TelemetryFreshness.STALE;
    }

    public boolean isDisqualified() {
        return this.quality == ReadingQuality.INVALID || this.freshness == TelemetryFreshness.MISSING;
    }
}
