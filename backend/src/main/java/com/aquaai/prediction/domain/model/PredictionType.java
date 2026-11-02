package com.aquaai.prediction.domain.model;

/**
 * Standard classification of predictive intelligence forecasts supported by the platform.
 */
public enum PredictionType {
    /**
     * Trajectory projection of soil moisture level based on valid recent telemetry,
     * rate of depletion, crop coefficient Kc, and reference evapotranspiration ET0.
     */
    SOIL_MOISTURE_FORECAST,

    /**
     * Estimation of when a parcel is likely to cross management allowed depletion (MAD)
     * and require an irrigation event, including estimated volumetric need.
     */
    IRRIGATION_NEED_FORECAST,

    /**
     * Aggregated volumetric water demand projection (expected, lower, upper bounds)
     * over a specified horizon for water budgeting.
     */
    WATER_DEMAND_FORECAST,

    /**
     * Comparative evaluation of projected water demand against remaining seasonal
     * water quota entitlement.
     */
    WATER_AVAILABILITY_RISK,

    /**
     * Multi-factor risk forecast for approved or scheduled smart irrigation plans
     * evaluating hydraulic safety, quota headroom, telemetry freshness, and delay.
     */
    IRRIGATION_PLAN_RISK,

    /**
     * Forecast of sensor reliability and failure drift based on historical
     * telemetry staleness, invalid readings, and offline intervals over 7-14 days.
     */
    SENSOR_RELIABILITY_FORECAST
}
