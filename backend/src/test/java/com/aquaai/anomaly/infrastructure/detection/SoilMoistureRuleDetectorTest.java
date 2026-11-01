package com.aquaai.anomaly.infrastructure.detection;

import com.aquaai.anomaly.domain.detection.DetectionContext;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoilMoistureRuleDetectorTest {

    private final SoilMoistureRuleDetector detector = new SoilMoistureRuleDetector();

    @Test
    @DisplayName("Detects SOIL_MOISTURE_CRITICAL when moisture is below critical 15% threshold")
    void shouldDetectCriticalSoilMoistureDeficit() {
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.SOIL_MOISTURE)
                .currentReadingValue(BigDecimal.valueOf(11.8))
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(Instant.now())
                .cropType("COTTON")
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.SOIL_MOISTURE_CRITICAL);
        assertThat(candidate.getSeverity()).isEqualTo(AnomalySeverity.CRITICAL);
        assertThat(candidate.getConfidence()).isGreaterThanOrEqualTo(BigDecimal.valueOf(0.90));
        assertThat(candidate.getExplanation()).contains("11.8%");
    }

    @Test
    @DisplayName("Detects rapid SOIL_MOISTURE_DROP when drop exceeds 10% between consecutive readings")
    void shouldDetectRapidMoistureDrop() {
        Instant now = Instant.now();
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.SOIL_MOISTURE)
                .currentReadingValue(BigDecimal.valueOf(18.0))
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(now)
                .recentReadings(List.of(new DetectionContext.HistoricalReadingPoint(BigDecimal.valueOf(31.5), ReadingQuality.VALID, now.minusSeconds(1800))))
                .cropType("WHEAT")
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.SOIL_MOISTURE_DROP);
        assertThat(candidate.getSeverity()).isEqualTo(AnomalySeverity.HIGH);
    }

    @Test
    @DisplayName("Skips detection if reading quality is INVALID (false-positive prevention)")
    void shouldSkipInvalidQualityReadings() {
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.SOIL_MOISTURE)
                .currentReadingValue(BigDecimal.valueOf(5.0))
                .currentReadingQuality(ReadingQuality.INVALID)
                .currentReadingTime(Instant.now())
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);
        assertThat(candidates).isEmpty();
    }
}
