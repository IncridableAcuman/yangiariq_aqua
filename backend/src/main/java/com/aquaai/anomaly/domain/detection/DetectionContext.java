package com.aquaai.anomaly.domain.detection;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Contextual payload provided to an AnomalyDetector for evaluation.
 */
@Getter
@Builder
public class DetectionContext {

    private final UUID fieldId;
    private final UUID sensorId;
    private final SensorType sensorType;
    private final BigDecimal currentReadingValue;
    private final ReadingQuality currentReadingQuality;
    private final Instant currentReadingTime;
    private final boolean isSimulated;
    private final List<HistoricalReadingPoint> recentReadings;
    private final BigDecimal baselineMean;
    private final BigDecimal baselineStdDev;
    private final AnomalyRuleConfig ruleConfig;
    private final boolean hasActiveIrrigationEvent;
    private final BigDecimal linePressureBar; // optional simultaneous line pressure
    private final String cropType;
    private final String soilType;

    public List<HistoricalReadingPoint> getRecentReadings() {
        return recentReadings != null ? Collections.unmodifiableList(recentReadings) : Collections.emptyList();
    }

    public record HistoricalReadingPoint(
            BigDecimal value,
            ReadingQuality quality,
            Instant measuredAt
    ) {
    }
}
