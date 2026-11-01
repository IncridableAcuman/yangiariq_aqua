package com.aquaai.anomaly.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aquaai.anomaly")
public class AnomalyProperties {

    private boolean enabled = true;
    private int cooldownMinutes = 30;
    private boolean deduplicationEnabled = true;
    private int historicalWindowHours = 24;
    private int minimumReadingsForStats = 5;
    private double zScoreThreshold = 3.0;
    private int offlineThresholdMinutes = 30;
    private boolean notificationEnabled = true;
    private boolean simulationAlertsEnabled = false; // Refinement #5: simulation isolation
}
