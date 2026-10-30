package com.aquaai.sensor.infrastructure;

import com.aquaai.sensor.domain.SensorProvider;
import com.aquaai.sensor.domain.SensorReading;
import com.aquaai.sensor.domain.SensorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.sensor.provider", havingValue = "simulated", matchIfMissing = true)
public class SimulatedSensorProvider implements SensorProvider {

    private final Random random = new Random();

    @Override
    public String getProviderType() {
        return "SIMULATED_SENSOR_PROVIDER";
    }

    @Override
    public SensorReading getLatestReading(UUID sensorId) {
        log.debug("Generating simulated telemetry for sensor: {}", sensorId);

        // Typical soil moisture in Yangiariq fields ranges between 40% and 75%
        double simulatedMoisture = 40.0 + (random.nextDouble() * 35.0);
        BigDecimal value = BigDecimal.valueOf(simulatedMoisture).setScale(2, RoundingMode.HALF_UP);

        return SensorReading.builder()
                .sensorId(sensorId)
                .fieldId(UUID.randomUUID())
                .sensorType(SensorType.SOIL_MOISTURE)
                .value(value)
                .unit("%")
                .batteryLevelPercent(BigDecimal.valueOf(94.5))
                .measuredAt(Instant.now())
                .providerSource(getProviderType())
                .build();
    }

    @Override
    public List<SensorReading> getReadingsForField(UUID fieldId) {
        log.debug("Generating simulated telemetry series for field: {}", fieldId);

        SensorReading moisture = SensorReading.builder()
                .sensorId(UUID.randomUUID())
                .fieldId(fieldId)
                .sensorType(SensorType.SOIL_MOISTURE)
                .value(BigDecimal.valueOf(56.80))
                .unit("%")
                .batteryLevelPercent(BigDecimal.valueOf(92.0))
                .measuredAt(Instant.now())
                .providerSource(getProviderType())
                .build();

        SensorReading flow = SensorReading.builder()
                .sensorId(UUID.randomUUID())
                .fieldId(fieldId)
                .sensorType(SensorType.WATER_FLOW)
                .value(BigDecimal.valueOf(14.25))
                .unit("liters/sec")
                .batteryLevelPercent(BigDecimal.valueOf(88.0))
                .measuredAt(Instant.now())
                .providerSource(getProviderType())
                .build();

        return List.of(moisture, flow);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
