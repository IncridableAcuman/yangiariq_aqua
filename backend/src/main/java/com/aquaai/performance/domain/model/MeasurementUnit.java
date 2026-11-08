package com.aquaai.performance.domain.model;

public enum MeasurementUnit {
    M3("m³"),
    M3_HA("m³/ha"),
    PERCENT("%"),
    HOURS("hours"),
    DAYS("days"),
    COUNT("count"),
    RATIO("ratio"),
    HECTARES("ha");

    private final String symbol;

    MeasurementUnit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
