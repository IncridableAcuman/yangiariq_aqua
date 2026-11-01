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

class IrrigationFailureDetectorTest {

    private final IrrigationFailureDetector detector = new IrrigationFailureDetector();

    @Test
    @DisplayName("Refinement #2: Confirms multi-metric pipe burst when flow is high and line pressure drops below 1.0 bar")
    void shouldDetectMultiMetricPipeBurst() {
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.WATER_FLOW)
                .currentReadingValue(BigDecimal.valueOf(62.0)) // High flow
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(Instant.now())
                .linePressureBar(BigDecimal.valueOf(0.65))    // Severe pressure drop
                .hasActiveIrrigationEvent(true)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.SUSPECTED_PIPE_BURST);
        assertThat(candidate.getSeverity()).isEqualTo(AnomalySeverity.CRITICAL);
        assertThat(candidate.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.92));
        assertThat(candidate.getEvidenceList()).hasSize(2);
    }

    @Test
    @DisplayName("Refinement #2: Classifies pipe burst as SUSPECTED with lower confidence when pressure corroboration is absent")
    void shouldClassifyPipeBurstAsSuspectedWhenPressureAbsent() {
        DetectionContext context = DetectionContext.builder()
                .fieldId(UUID.randomUUID())
                .sensorId(UUID.randomUUID())
                .sensorType(SensorType.WATER_FLOW)
                .currentReadingValue(BigDecimal.valueOf(58.0)) // High flow
                .currentReadingQuality(ReadingQuality.VALID)
                .currentReadingTime(Instant.now())
                .linePressureBar(null)                         // No pressure sensor
                .hasActiveIrrigationEvent(true)
                .build();

        List<AnomalyCandidate> candidates = detector.detect(context);

        assertThat(candidates).hasSize(1);
        AnomalyCandidate candidate = candidates.get(0);
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.SUSPECTED_PIPE_BURST);
        assertThat(candidate.getSeverity()).isEqualTo(AnomalySeverity.HIGH);
        assertThat(candidate.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.65));
        assertThat(candidate.getExplanation()).contains("bosim datchigi maʼlumotlari mavjud emas");
    }

    @Test
    @DisplayName("Detects IRRIGATION_FAILURE when active irrigation event has zero flow")
    void shouldDetectIrrigationFailure() {
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
        assertThat(candidate.getAnomalyType()).isEqualTo(AnomalyType.IRRIGATION_FAILURE);
        assertThat(candidate.getSeverity()).isEqualTo(AnomalySeverity.HIGH);
        assertThat(candidate.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.95));
    }
}
