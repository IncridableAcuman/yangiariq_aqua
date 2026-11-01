package com.aquaai.sensor.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standard agronomic measurement units supported by agricultural telemetry sensors in Yangiariq district.
 */
@Getter
@RequiredArgsConstructor
public enum MeasurementUnit {

    PERCENT("%", "Percentage"),
    CUBIC_METER_PER_HOUR("m³/h", "Cubic meters per hour"),
    LITERS_PER_SECOND("L/s", "Liters per second"),
    CELSIUS("°C", "Degree Celsius"),
    METERS("m", "Meters"),
    MILLIMETERS("mm", "Millimeters"),
    BAR("bar", "Bar"),
    MILLIBAR("mbar", "Millibar"),
    DECISIEMENS_PER_METER("dS/m", "Decisiemens per meter");

    private final String symbol;
    private final String displayName;

    @Override
    public String toString() {
        return this.symbol;
    }
}
