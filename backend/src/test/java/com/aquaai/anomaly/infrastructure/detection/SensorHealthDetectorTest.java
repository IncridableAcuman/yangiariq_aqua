package com.aquaai.anomaly.infrastructure.detection;

import com.aquaai.anomaly.domain.detection.DetectionContext;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SensorHealthDetectorTest {

    private final SensorHealthDetector detector = new SensorHealthDetector();

    @Test
    @DisplayName("Detects SENSOR_STUCK when last 5 readings are identical")
    void shouldDetectStuckSensor() {
        Instant now = Instant.now();
        List<DetectionContext.HistoricalReadingPoint> recent = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            recent.add(new DetectionContext.HistoricalReadingPoint(BigDecimal.valueOf(24.50), ReadingQuality.VALID, now.minusSeconds(i * 300)));
        }

        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.SOIL_MOISTURE)
                .currentReadingValue(BigDecimal.valueOf(24.50))
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(now)
                .recentReadings(recent)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.SENSOR_STUCK);
        assertThat(candidate.getExplanation()).contains("qotib qolgan");
    }

    @Test
    @DisplayName("Refinement #3: Classifies drift as POSSIBLE_SENSOR_DRIFT without claiming confirmed calibration failure")
    void shouldClassifyDriftAsPossibleSensorDrift() {
        Instant now = Instant.now();
        List<DetectionContext.HistoricalReadingPoint> recent = new ArrayList<>();
        // Baseline is 20.0, recent readings average around 30.0 with natural jitter (> 35% drift)
        double[] jitter = {29.5, 30.2, 29.8, 30.5, 30.1, 29.9, 30.0, 30.4, 29.7, 30.3};
        for (int i = 0; i < jitter.length; i++) {
            recent.add(new DetectionContext.HistoricalReadingPoint(BigDecimal.valueOf(jitter[i]), ReadingQuality.VALID, now.minusSeconds((i + 1) * 300)));
        }

        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.SOIL_MOISTURE)
                .currentReadingValue(BigDecimal.valueOf(30.0))
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(now)
                .baselineMean(BigDecimal.valueOf(20.0))
                .recentReadings(recent)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.POSSIBLE_SENSOR_DRIFT);
        assertThat(candidate.getExplanation()).contains("ehtimoliy dreyf");
    }
}
