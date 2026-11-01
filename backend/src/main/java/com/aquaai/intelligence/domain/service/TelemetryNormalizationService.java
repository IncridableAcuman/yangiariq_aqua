package com.aquaai.intelligence.domain.service;

import com.aquaai.intelligence.domain.model.TelemetryFreshness;
import com.aquaai.intelligence.domain.model.TelemetrySignal;
import com.aquaai.sensor.domain.model.SensorReading;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Normalizes physical SensorReading domain objects into canonical TelemetrySignal entities.
 */
public class TelemetryNormalizationService {

    private final FreshnessEvaluator freshnessEvaluator;

    public TelemetryNormalizationService(FreshnessEvaluator freshnessEvaluator) {
        this.freshnessEvaluator = freshnessEvaluator != null ? freshnessEvaluator : new FreshnessEvaluator();
    }

    public List<TelemetrySignal> normalize(List<SensorReading> readings, Instant evaluationTime) {
        if (readings == null || readings.isEmpty()) {
            return Collections.emptyList();
        }

        Instant now = evaluationTime != null ? evaluationTime : Instant.now();
        List<TelemetrySignal> signals = new ArrayList<>();

        for (SensorReading reading : readings) {
            if (reading == null) continue;

            TelemetryFreshness freshness = freshnessEvaluator.evaluate(
                    reading.getType(),
                    reading.getMeasuredAt(),
                    now
            );

            signals.add(new TelemetrySignal(
                    reading.getFieldId(),
                    reading.getSensorId(),
                    reading.getType(),
                    reading.getValue(),
                    reading.getUnit(),
                    reading.getQuality(),
                    reading.getMeasuredAt(),
                    freshness
            ));
        }

        return signals;
    }
}
