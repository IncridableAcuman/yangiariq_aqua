package com.aquaai.recommendation.domain.model;

/**
 * Type-safe classification of agronomic decision-support recommendations.
 */
public enum RecommendationType {
    IRRIGATE_NOW,
    DELAY_IRRIGATION,
    REDUCE_WATER_VOLUME,
    AVOID_IRRIGATION,
    INSPECT_PIPE_BURST,
    INSPECT_SENSOR,
    MONITOR_SOIL_MOISTURE,
    MANAGE_SALINITY,
    PREPARE_HEAT_STRESS,
    PREPARE_FROST,
    INVESTIGATE_OVERCONSUMPTION
}
