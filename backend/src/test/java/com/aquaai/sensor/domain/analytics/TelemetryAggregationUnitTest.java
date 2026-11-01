package com.aquaai.sensor.domain.analytics;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelemetryAggregationUnitTest {

    @Test
    @DisplayName("AggregationInterval parses codes correctly and truncates timestamps accurately")
    void aggregationInterval_parsesAndTruncates() {
        assertThat(AggregationInterval.fromCode("5m")).isEqualTo(AggregationInterval.FIVE_MINUTES);
        assertThat(AggregationInterval.fromCode("15m")).isEqualTo(AggregationInterval.FIFTEEN_MINUTES);
        assertThat(AggregationInterval.fromCode("1h")).isEqualTo(AggregationInterval.HOURLY);
        assertThat(AggregationInterval.fromCode("1d")).isEqualTo(AggregationInterval.DAILY);
        assertThat(AggregationInterval.fromCode("1w")).isEqualTo(AggregationInterval.WEEKLY);
        assertThat(AggregationInterval.fromCode(null)).isEqualTo(AggregationInterval.HOURLY);
        assertThat(AggregationInterval.fromCode("")).isEqualTo(AggregationInterval.HOURLY);

        assertThatThrownBy(() -> AggregationInterval.fromCode("99m"))
                .isInstanceOf(IllegalArgumentException.class);

        // Truncate to hour
        Instant time = Instant.parse("2026-11-01T09:27:45Z");
        Instant truncatedHour = AggregationInterval.HOURLY.truncate(time);
        assertThat(truncatedHour).isEqualTo(Instant.parse("2026-11-01T09:00:00Z"));

        // Truncate to 15m
        Instant truncated15m = AggregationInterval.FIFTEEN_MINUTES.truncate(time);
        assertThat(truncated15m).isEqualTo(Instant.parse("2026-11-01T09:15:00Z"));

        // Truncate to 5m
        Instant truncated5m = AggregationInterval.FIVE_MINUTES.truncate(time);
        assertThat(truncated5m).isEqualTo(Instant.parse("2026-11-01T09:25:00Z"));
    }

    @Test
    @DisplayName("AggregationPolicy enforces modality-specific mathematical validity")
    void aggregationPolicy_enforcesRules() {
        // Soil moisture: avg, min, max are meaningful; sum is meaningless
        assertThat(AggregationPolicy.isAverageMeaningful(SensorType.SOIL_MOISTURE)).isTrue();
        assertThat(AggregationPolicy.isSumMeaningful(SensorType.SOIL_MOISTURE)).isFalse();
        assertThat(AggregationPolicy.isMinMaxMeaningful(SensorType.SOIL_MOISTURE)).isTrue();

        // Water flow: avg, min, max are meaningful; sum of flow rates is not cumulative volume
        assertThat(AggregationPolicy.isAverageMeaningful(SensorType.WATER_FLOW)).isTrue();
        assertThat(AggregationPolicy.isSumMeaningful(SensorType.WATER_FLOW)).isFalse();

        // Rainfall: sum is meaningful (accumulation); average of increments is not
        assertThat(AggregationPolicy.isSumMeaningful(SensorType.RAINFALL)).isTrue();
        assertThat(AggregationPolicy.isAverageMeaningful(SensorType.RAINFALL)).isFalse();
    }

    @Test
    @DisplayName("TelemetryAggregate enforces invariants and scales numbers to 4 decimal places")
    void telemetryAggregate_enforcesInvariants() {
        UUID fieldId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant start = Instant.parse("2026-11-01T09:00:00Z");
        Instant end = Instant.parse("2026-11-01T10:00:00Z");

        TelemetryAggregate agg = new TelemetryAggregate(
                fieldId,
                sensorId,
                SensorType.SOIL_MOISTURE,
                AggregationInterval.HOURLY,
                start,
                end,
                new BigDecimal("48.5"),
                new BigDecimal("45.1"),
                new BigDecimal("51.9"),
                null,
                new BigDecimal("45.1"),
                start,
                new BigDecimal("50.0"),
                end.minusSeconds(60),
                12,
                MeasurementUnit.PERCENT
        );

        assertThat(agg.average()).isEqualByComparingTo(new BigDecimal("48.5000"));
        assertThat(agg.minimum()).isEqualByComparingTo(new BigDecimal("45.1000"));
        assertThat(agg.maximum()).isEqualByComparingTo(new BigDecimal("51.9000"));
        assertThat(agg.readingCount()).isEqualTo(12);

        // Invalid: start after end
        assertThatThrownBy(() -> new TelemetryAggregate(
                fieldId, sensorId, SensorType.SOIL_MOISTURE, AggregationInterval.HOURLY,
                end, start, null, null, null, null, null, null, null, null, 0, MeasurementUnit.PERCENT
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("DataQualityMetrics calculates validity percentage correctly")
    void dataQualityMetrics_calculatesPercentages() {
        DataQualityMetrics metrics = DataQualityMetrics.calculate(100, 95, 3, 2, 0, 1);
        assertThat(metrics.validPercentage()).isEqualTo(95.0);
        assertThat(metrics.validReadings()).isEqualTo(95);
        assertThat(metrics.suspectReadings()).isEqualTo(3);
        assertThat(metrics.invalidReadings()).isEqualTo(2);

        DataQualityMetrics empty = DataQualityMetrics.calculate(0, 0, 0, 0, 0, 0);
        assertThat(empty.validPercentage()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("SensorAvailabilitySummary counts hardware and communicating sensors")
    void sensorAvailabilitySummary_evaluatesCounts() {
        SensorAvailabilitySummary summary = new SensorAvailabilitySummary(10, 8, 1, 1, 0, 7, 1);
        assertThat(summary.totalSensors()).isEqualTo(10);
        assertThat(summary.activeCount()).isEqualTo(8);
        assertThat(summary.communicatingCount()).isEqualTo(7);
        assertThat(summary.unresponsiveCount()).isEqualTo(1);
    }
}
