package com.aquaai.prediction;

import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.service.FeatureExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureExtractorUnitTest {

    private final FeatureExtractor featureExtractor = new FeatureExtractor();

    @Test
    @DisplayName("Extracts all 14 standardized features with correct categorical encoding and slope")
    void extractsStandardizedFeatures() {
        UUID fieldId = UUID.randomUUID();
        FieldMetadata field = new FieldMetadata(
                fieldId, UUID.randomUUID(), "Parcel 101",
                "COTTON", "LOAM", "DRIP",
                BigDecimal.valueOf(12.5), true
        );

        Instant now = Instant.parse("2026-07-15T12:00:00Z");

        List<TelemetryObservation> observations = List.of(
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(28.0), "VALID", now.minus(Duration.ofHours(24))),
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(26.0), "VALID", now.minus(Duration.ofHours(12))),
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(24.0), "VALID", now)
        );

        WeatherContext weather = new WeatherContext(
                BigDecimal.valueOf(34.5),
                BigDecimal.valueOf(38.0),
                BigDecimal.valueOf(6.2),
                BigDecimal.valueOf(0.0)
        );

        WaterBalance waterBalance = new WaterBalance(
                UUID.randomUUID(),
                BigDecimal.valueOf(50000.0),
                BigDecimal.valueOf(20000.0),
                BigDecimal.valueOf(30000.0),
                BigDecimal.ZERO,
                false
        );

        double[] features = featureExtractor.extractSoilMoistureFeatures(
                field, observations, weather, waterBalance,
                BigDecimal.valueOf(1500.0), now
        );

        assertThat(features).hasSize(FeatureExtractor.FEATURE_COUNT);

        // Feature 0: Current moisture
        assertThat(features[0]).isEqualTo(24.0);

        // Feature 1: Slope per hour (28% -> 24% over 24h = approx -0.1667 %/hr)
        assertThat(features[1]).isBetween(-0.18, -0.15);

        // Feature 2: Temp
        assertThat(features[2]).isEqualTo(34.5);

        // Feature 3: RH
        assertThat(features[3]).isEqualTo(38.0);

        // Feature 4: ET0
        assertThat(features[4]).isEqualTo(6.2);

        // Feature 5: Rain
        assertThat(features[5]).isEqualTo(0.0);

        // Feature 6: Crop code (Cotton = 1.0)
        assertThat(features[6]).isEqualTo(1.0);

        // Feature 7: Soil code (Loam = 3.0)
        assertThat(features[7]).isEqualTo(3.0);

        // Feature 8: Irrigation method (Drip = 1.0)
        assertThat(features[8]).isEqualTo(1.0);

        // Feature 9: Area
        assertThat(features[9]).isEqualTo(12.5);

        // Feature 10: Recent irrigation volume
        assertThat(features[10]).isEqualTo(1500.0);

        // Feature 11: Headroom % (30000 / 50000 = 60%)
        assertThat(features[11]).isEqualTo(60.0);

        // Feature 12: Season (July = Summer = 2.0)
        assertThat(features[12]).isEqualTo(2.0);

        // Feature 13: Telemetry quality score (3 of 3 valid = 1.0)
        assertThat(features[13]).isEqualTo(1.0);
    }
}
