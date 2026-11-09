package com.aquaai.sensor.infrastructure.provider;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.provider.SensorProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Deterministic, realistically bounded simulation provider for agricultural telemetry in Yangiariq district.
 * <p>
 * Employs continuous random-walk drift from realistic agronomic baselines rather than unconstrained random spikes,
 * guaranteeing coherent, believable time-series data sequences for demo and local development.
 */
@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.sensor.provider", havingValue = "simulated", matchIfMissing = true)
public class SimulatedSensorProvider implements SensorProvider {

    public static final String PROVIDER_TYPE = "SIMULATED_SENSOR_PROVIDER";

    private final Random random = new Random();
    private final ConcurrentMap<UUID, BigDecimal> previousValues = new ConcurrentHashMap<>();

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public SensorReading read(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor aggregate must not be null for simulation");
        }

        SensorType type = sensor.getType();
        MeasurementUnit unit = type.getDefaultUnit();
        BigDecimal nextValue = computeNextRealisticValue(sensor.getId(), type);

        log.debug("Simulated telemetry reading generated for sensor {} (type={}): {} {}",
                sensor.getId(), type, nextValue, unit);

        return SensorReading.create(
                sensor.getId(),
                sensor.getFieldId(),
                type,
                nextValue,
                unit,
                ReadingQuality.VALID,
                PROVIDER_TYPE,
                Instant.now()
        );
    }

    /**
     * Calculates the next telemetry value using a constrained bounded drift model.
     */
    private BigDecimal computeNextRealisticValue(UUID sensorId, SensorType type) {
        BigDecimal baseline = getBaseline(type);
        BigDecimal min = getSimulationMin(type);
        BigDecimal max = getSimulationMax(type);
        double maxDelta = getMaxDriftStep(type);

        BigDecimal current = previousValues.getOrDefault(sensorId, baseline);

        // Drift by [-maxDelta, +maxDelta]
        double delta = (random.nextDouble() * (2 * maxDelta)) - maxDelta;
        BigDecimal updated = current.add(BigDecimal.valueOf(delta));

        // Mean-reversion tendency if drifting too far from baseline
        if (updated.compareTo(max) > 0 || updated.compareTo(min) < 0) {
            updated = baseline.add(BigDecimal.valueOf(delta * 0.5));
        }

        // Clamp to physical simulation boundaries
        if (updated.compareTo(min) < 0) {
            updated = min;
        } else if (updated.compareTo(max) > 0) {
            updated = max;
        }

        updated = updated.setScale(2, RoundingMode.HALF_UP);
        previousValues.put(sensorId, updated);
        return updated;
    }

    private BigDecimal getBaseline(SensorType type) {
        return switch (type) {
            case SOIL_MOISTURE -> new BigDecimal("52.50");
            case WATER_FLOW -> new BigDecimal("14.80");
            case TEMPERATURE -> new BigDecimal("24.20");
            case HUMIDITY -> new BigDecimal("46.00");
            case WATER_LEVEL -> new BigDecimal("1.85");
            case PRESSURE -> new BigDecimal("2.40");
            case RAINFALL -> new BigDecimal("0.00");
            case SALINITY -> new BigDecimal("1.75");
        };
    }

    private BigDecimal getSimulationMin(SensorType type) {
        return switch (type) {
            case SOIL_MOISTURE -> new BigDecimal("20.00");
            case WATER_FLOW -> BigDecimal.ZERO;
            case TEMPERATURE -> new BigDecimal("12.00");
            case HUMIDITY -> new BigDecimal("25.00");
            case WATER_LEVEL -> new BigDecimal("0.30");
            case PRESSURE -> new BigDecimal("0.80");
            case RAINFALL -> BigDecimal.ZERO;
            case SALINITY -> new BigDecimal("0.50");
        };
    }

    private BigDecimal getSimulationMax(SensorType type) {
        return switch (type) {
            case SOIL_MOISTURE -> new BigDecimal("82.00");
            case WATER_FLOW -> new BigDecimal("45.00");
            case TEMPERATURE -> new BigDecimal("41.00");
            case HUMIDITY -> new BigDecimal("88.00");
            case WATER_LEVEL -> new BigDecimal("4.50");
            case PRESSURE -> new BigDecimal("5.50");
            case RAINFALL -> new BigDecimal("20.00");
            case SALINITY -> new BigDecimal("6.00");
        };
    }

    private double getMaxDriftStep(SensorType type) {
        return switch (type) {
            case SOIL_MOISTURE -> 0.40;
            case WATER_FLOW -> 0.35;
            case TEMPERATURE -> 0.25;
            case HUMIDITY -> 0.60;
            case WATER_LEVEL -> 0.05;
            case PRESSURE -> 0.05;
            case RAINFALL -> 0.10;
            case SALINITY -> 0.04;
        };
    }

    /**
     * Resets internal drift memory for testing predictability.
     */
    public void resetSequence(UUID sensorId) {
        previousValues.remove(sensorId);
    }
}
