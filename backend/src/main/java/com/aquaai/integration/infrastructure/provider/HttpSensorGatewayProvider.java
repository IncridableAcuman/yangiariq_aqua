package com.aquaai.integration.infrastructure.provider;

import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.provider.SensorProvider;
import com.aquaai.shared.domain.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Real-world edge HTTP gateway provider contract for physical IoT telemetry gateways.
 * Operates safely when physical hardware is pending deployment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpSensorGatewayProvider implements SensorProvider {

    public static final String PROVIDER_TYPE = "HTTP_SENSOR_GATEWAY";

    private final DataSourceRepository dataSourceRepository;

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean isAvailable() {
        Optional<DataSource> ds = dataSourceRepository.findBySourceCode(PROVIDER_TYPE);
        return ds.isPresent() && ds.get().getStatus() == DataSourceStatus.ACTIVE;
    }

    @Override
    public SensorReading read(Sensor sensor) {
        if (!isAvailable()) {
            throw new BusinessRuleException(
                    "Physical edge hardware gateway for " + sensor.getDeviceIdentifier() + " is currently AWAITING_HARDWARE",
                    "GATEWAY_NOT_CONNECTED"
            );
        }

        // When physical edge device pushes data, readings are ingested via /api/v1/data-ingestion/telemetry
        throw new UnsupportedOperationException(
                "Direct pull telemetry is not supported for push-based edge IoT gateway. Awaiting webhook ingestion."
        );
    }
}
