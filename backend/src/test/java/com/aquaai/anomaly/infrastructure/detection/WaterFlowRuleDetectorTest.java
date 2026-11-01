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

class WaterFlowRuleDetectorTest {

    private final WaterFlowRuleDetector detector = new WaterFlowRuleDetector();

    @Test
    @DisplayName("Detects WATER_FLOW_ZERO when active irrigation event is running but flow is zero")
    void shouldDetectZeroFlowDuringActiveIrrigation() {
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.WATER_FLOW)
                .currentReadingValue(BigDecimal.ZERO)
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(Instant.now())
                .hasActiveIrrigationEvent(true)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.WATER_FLOW_ZERO);
        assertThat(candidate.getSeverity()).isEqualTo(AnomalySeverity.HIGH);
        assertThat(candidate.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.95));
    }

    @Test
    @DisplayName("Detects WATER_FLOW_SPIKE when flow exceeds default 45 m3/h threshold")
    void shouldDetectFlowSpike() {
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.WATER_FLOW)
                .currentReadingValue(BigDecimal.valueOf(55.4))
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(Instant.now())
                .baselineMean(BigDecimal.valueOf(22.0))
                .hasActiveIrrigationEvent(true)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.WATER_FLOW_SPIKE);
        assertThat(candidate.getSeverity()).isEqualTo(AnomalySeverity.HIGH);
    }
}
