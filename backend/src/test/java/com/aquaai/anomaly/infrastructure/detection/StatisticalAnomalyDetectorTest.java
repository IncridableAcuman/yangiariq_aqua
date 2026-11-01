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

class StatisticalAnomalyDetectorTest {

    private final StatisticalAnomalyDetector detector = new StatisticalAnomalyDetector();

    @Test
    @DisplayName("Detects SENSOR_OUTLIER when |Z-score| >= 3.0")
    void shouldDetectZScoreOutlier() {
        Instant now = Instant.now();
        List<DetectionContext.HistoricalReadingPoint> recent = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            recent.add(new DetectionContext.HistoricalReadingPoint(BigDecimal.valueOf(20.0), ReadingQuality.VALID, now.minusSeconds(i * 300)));
        }

        // μ = 20.0, σ = 2.0, X = 32.0 -> Z = (32 - 20) / 2 = 6.0 >= 3.0
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.TEMPERATURE)
                .currentReadingValue(BigDecimal.valueOf(32.0))
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(now)
                .baselineMean(BigDecimal.valueOf(20.0))
                .baselineStdDev(BigDecimal.valueOf(2.0))
                .recentReadings(recent)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.SENSOR_OUTLIER);
        assertThat(candidate.getExplanation()).contains("Z-score");
    }

    @Test
    @DisplayName("Handles zero standard deviation safely without dividing by zero")
    void shouldHandleZeroStdDevSafely() {
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.TEMPERATURE)
                .currentReadingValue(BigDecimal.valueOf(25.0))
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(Instant.now())
                .baselineMean(BigDecimal.valueOf(20.0))
                .baselineStdDev(BigDecimal.ZERO)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);
        assertThat(candidates).isEmpty();
    }
}
