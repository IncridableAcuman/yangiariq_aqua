package com.aquaai.prediction.infrastructure.provider;

import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.port.PredictionModelProvider;
import com.aquaai.prediction.domain.service.FeatureExtractor;
import com.aquaai.prediction.domain.service.RidgeRegressionTrainer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JVM-native ML Regression Model Provider executing governed candidate configurations.
 * Satisfies Hexagonal architecture and ArchUnit Rule 3.
 */
@Slf4j
@Component
public class MLRegressionPredictionModelProvider implements PredictionModelProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getProviderName() {
        return "MLRegressionProvider";
    }

    @Override
    public String getModelType() {
        return "REGRESSION_RIDGE_HYBRID";
    }

    @Override
    public boolean supports(PredictionType predictionType) {
        return predictionType == PredictionType.SOIL_MOISTURE_FORECAST;
    }

    @Override
    public Optional<Double> predictSoilMoisture(
            FieldMetadata field,
            PredictionHorizon horizon,
            double currentMoisture,
            double slope24h,
            WeatherContext weather,
            double recentIrrigationVolume7d,
            double waterHeadroomPercent,
            double telemetryQualityScore,
            String configurationJson
    ) {
        if (configurationJson == null || configurationJson.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(configurationJson);
            if (!root.has("weights") || !root.has("intercept")) {
                return Optional.empty();
            }

            JsonNode weightsNode = root.get("weights");
            double intercept = root.get("intercept").asDouble();

            int m = weightsNode.size();
            if (m < FeatureExtractor.FEATURE_COUNT) {
                return Optional.empty();
            }

            double[] weights = new double[m];
            for (int i = 0; i < m; i++) {
                weights[i] = weightsNode.get(i).asDouble();
            }

            double[] means = new double[m];
            if (root.has("featureMeans")) {
                JsonNode meansNode = root.get("featureMeans");
                for (int i = 0; i < Math.min(m, meansNode.size()); i++) {
                    means[i] = meansNode.get(i).asDouble();
                }
            }

            double[] stdDevs = new double[m];
            if (root.has("featureStdDevs")) {
                JsonNode stdNode = root.get("featureStdDevs");
                for (int i = 0; i < Math.min(m, stdNode.size()); i++) {
                    stdDevs[i] = stdNode.get(i).asDouble();
                }
            }

            // Construct 14-dimensional feature vector
            double[] features = new double[FeatureExtractor.FEATURE_COUNT];
            features[0] = currentMoisture;
            features[1] = slope24h;
            features[2] = (weather != null && weather.temperatureC() != null) ? weather.temperatureC().doubleValue() : 28.0;
            features[3] = (weather != null && weather.relativeHumidityPercent() != null) ? weather.relativeHumidityPercent().doubleValue() : 45.0;
            features[4] = (weather != null && weather.referenceEt0MmDay() != null) ? weather.referenceEt0MmDay().doubleValue() : 5.0;
            features[5] = (weather != null && weather.forecastRainMm() != null) ? weather.forecastRainMm().doubleValue() : 0.0;
            features[6] = encodeCrop(field != null ? field.cropType() : null);
            features[7] = encodeSoil(field != null ? field.soilType() : null);
            features[8] = encodeMethod(field != null ? field.irrigationMethod() : null);
            features[9] = (field != null && field.areaHectares() != null) ? field.areaHectares().doubleValue() : 5.0;
            features[10] = recentIrrigationVolume7d;
            features[11] = waterHeadroomPercent;
            features[12] = 2.0; // summer/growing season default
            features[13] = telemetryQualityScore;

            double prediction = RidgeRegressionTrainer.predict(features, weights, intercept, means, stdDevs);
            if (Double.isNaN(prediction) || Double.isInfinite(prediction)) {
                return Optional.empty();
            }

            return Optional.of(prediction);
        } catch (Exception ex) {
            log.warn("Failed to evaluate ML regression prediction configuration: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private double encodeCrop(String c) {
        if (c == null) return 0.0;
        String u = c.toUpperCase();
        if (u.contains("COTTON")) return 1.0;
        if (u.contains("WHEAT")) return 2.0;
        if (u.contains("MAIZE")) return 3.0;
        if (u.contains("ORCHARD")) return 4.0;
        return 0.0;
    }

    private double encodeSoil(String s) {
        if (s == null) return 0.0;
        String u = s.toUpperCase();
        if (u.contains("SANDY")) return 1.0;
        if (u.contains("CLAY")) return 2.0;
        if (u.contains("LOAM")) return 3.0;
        if (u.contains("SILT")) return 4.0;
        return 0.0;
    }

    private double encodeMethod(String m) {
        if (m == null) return 0.0;
        String u = m.toUpperCase();
        if (u.contains("DRIP")) return 1.0;
        if (u.contains("SPRINKLER")) return 2.0;
        if (u.contains("FURROW")) return 3.0;
        if (u.contains("FLOOD")) return 4.0;
        return 0.0;
    }
}
