package com.aquaai.irrigation.domain.model;

/**
 * Classification of constraints affecting smart irrigation scheduling.
 */
public enum ConstraintType {
    HYDRAULIC_SAFETY,
    WATER_QUOTA,
    INFRASTRUCTURE_CAPACITY,
    AGRONOMIC_DEFICIT,
    WEATHER_WINDOW
}
