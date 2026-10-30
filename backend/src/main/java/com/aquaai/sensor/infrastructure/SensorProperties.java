package com.aquaai.sensor.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.sensor")
public class SensorProperties {

    private String provider = "simulated";
    private long simulationIntervalMs = 30_000L;
}
