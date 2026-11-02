package com.aquaai.analytics.api.dto;

import com.aquaai.analytics.domain.model.DataConfidenceLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Builder
public class TelemetryQualityDto {
    private final int totalSensors;
    private final int activeSensors;
    private final int staleSensors;
    private final int offlineSensors;
    private final long totalReadings;
    private final long validReadings;
    private final long suspectReadings;
    private final long invalidReadings;
    private final BigDecimal telemetryCoveragePercent;
    private final DataConfidenceLevel dataConfidence;
    private final String coverageDefinition;
    private final String qualityNotice;

    @JsonProperty("onlineSensors")
    public int getOnlineSensors() {
        return activeSensors;
    }

    @JsonProperty("degradedSensors")
    public int getDegradedSensors() {
        return staleSensors;
    }

    @JsonProperty("totalReadingsCount")
    public long getTotalReadingsCount() {
        return totalReadings;
    }

    @JsonProperty("validReadingsCount")
    public long getValidReadingsCount() {
        return validReadings;
    }

    @JsonProperty("validReadingPercent")
    public BigDecimal getValidReadingPercent() {
        return telemetryCoveragePercent;
    }

    @JsonProperty("sensorAvailabilityPercent")
    public BigDecimal getSensorAvailabilityPercent() {
        return totalSensors > 0 ?
                BigDecimal.valueOf((activeSensors * 100.0) / totalSensors).setScale(1, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }
}
