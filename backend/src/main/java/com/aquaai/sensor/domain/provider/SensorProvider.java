package com.aquaai.sensor.domain.provider;

import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;

/**
 * Domain port abstraction for telemetry acquisition providers (Simulated, MQTT, HTTP, LoRaWAN).
 * The domain and application layers remain entirely agnostic of whether telemetry originates
 * from mathematical simulation or physical edge hardware.
 */
public interface SensorProvider {

    /**
     * Acquires or generates an instantaneous telemetry reading for the given sensor aggregate.
     *
     * @param sensor Sensor device aggregate
     * @return Fresh immutable telemetry reading
     */
    SensorReading read(Sensor sensor);

    /**
     * Unique identifier tag of the provider implementation.
     */
    String getProviderType();

    /**
     * Indicates whether the provider is currently operational.
     */
    boolean isAvailable();
}
