package com.aquaai.prediction.domain.model;

import java.util.List;

/**
 * Versioned schema defining the exact feature vector structure for predictive models.
 * Guarantees zero leakage: features are strictly computed from past operational data available at forecast time.
 */
public record FeatureSchemaVersion(
        String versionTag,
        int featureCount,
        List<FeatureDefinition> features
) {
    public record FeatureDefinition(
            String name,
            String type,
            String unit,
            String sourceTable,
            String calculationRule,
            String temporalSemantics
    ) {}

    public static final String SOIL_MOISTURE_SCHEMA_V1 = "FEAT_SOIL_MOISTURE_V1";

    public static FeatureSchemaVersion getSoilMoistureSchemaV1() {
        return new FeatureSchemaVersion(
                SOIL_MOISTURE_SCHEMA_V1,
                14,
                List.of(
                        new FeatureDefinition("soil_moisture_current", "NUMERICAL", "%", "sensor_readings", "Latest valid sensor observation value", "Measured at t <= T"),
                        new FeatureDefinition("soil_moisture_trend_24h", "NUMERICAL", "%/h", "sensor_readings", "Linear regression slope over past 24 hours", "Window [T-24h, T]"),
                        new FeatureDefinition("temperature_c", "NUMERICAL", "°C", "weather_snapshot", "Ambient air temperature", "Window [T-1h, T]"),
                        new FeatureDefinition("relative_humidity_percent", "NUMERICAL", "%", "weather_snapshot", "Relative atmospheric humidity", "Window [T-1h, T]"),
                        new FeatureDefinition("reference_et0_mm_day", "NUMERICAL", "mm/day", "weather_snapshot", "FAO-56 reference evapotranspiration rate", "Window [T-24h, T]"),
                        new FeatureDefinition("forecast_rain_mm", "NUMERICAL", "mm", "weather_forecast", "Cumulative forecasted precipitation", "Forecast for [T, T+horizon]"),
                        new FeatureDefinition("crop_type_code", "CATEGORICAL_ORDINAL", "code", "fields", "Crop classification (1=Cotton, 2=Wheat, 3=Maize, 4=Orchard)", "Static parcel metadata"),
                        new FeatureDefinition("soil_type_code", "CATEGORICAL_ORDINAL", "code", "fields", "Soil texture classification (1=Sandy loam, 2=Clay loam, 3=Loam, 4=Silty clay)", "Static parcel metadata"),
                        new FeatureDefinition("irrigation_method_code", "CATEGORICAL_ORDINAL", "code", "fields", "Application method (1=Drip, 2=Sprinkler, 3=Furrow, 4=Flood)", "Static parcel metadata"),
                        new FeatureDefinition("field_area_hectares", "NUMERICAL", "ha", "fields", "Total registered parcel boundary area", "Static parcel metadata"),
                        new FeatureDefinition("recent_irrigation_volume_7d", "NUMERICAL", "m³", "irrigation_events", "Cumulative actual water volume applied in past 7 days", "Window [T-7d, T]"),
                        new FeatureDefinition("water_headroom_percent", "NUMERICAL", "%", "water_allocations", "Remaining volumetric quota percentage", "Balance at t <= T"),
                        new FeatureDefinition("season_code", "CATEGORICAL_ORDINAL", "code", "calendar", "Agricultural season (1=Spring, 2=Summer, 3=Autumn, 4=Winter)", "Calendar context at T"),
                        new FeatureDefinition("telemetry_quality_score", "NUMERICAL", "ratio", "sensor_readings", "Fraction of VALID sensor readings in past 72h", "Window [T-72h, T]")
                )
        );
    }
}
