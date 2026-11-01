package com.aquaai.anomaly.domain.model;

import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnomalyDomainUnitTest {

    @Test
    @DisplayName("Factory method initializes Anomaly with DETECTED status and observationCount=1")
    void shouldCreateAnomalyWithDetectedStatus() {
        UUID fieldId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        AnomalyCandidate candidate = new AnomalyCandidate(
                AnomalyType.SOIL_MOISTURE_CRITICAL,
                AnomalySeverity.CRITICAL,
                BigDecimal.valueOf(12.5),
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(-12.5),
                BigDecimal.valueOf(0.95),
                "Critical soil moisture deficit",
                "Soil moisture dropped below 15% threshold",
                "RULE_ENGINE",
                List.of(AnomalyEvidence.of("THRESHOLD", "SOIL_MOISTURE", BigDecimal.valueOf(12.5), BigDecimal.valueOf(25.0), Instant.now(), null)),
                false
        );

        Instant now = Instant.now();
        Anomaly anomaly = Anomaly.create(fieldId, sensorId, SensorType.SOIL_MOISTURE, candidate, now);

        assertThat(anomaly.getId()).isNotNull();
        assertThat(anomaly.getFieldId()).isEqualTo(fieldId);
        assertThat(anomaly.getSensorId()).isEqualTo(sensorId);
        assertThat(anomaly.getSensorType()).isEqualTo(SensorType.SOIL_MOISTURE);
        assertThat(anomaly.getAnomalyType()).isEqualTo(AnomalyType.SOIL_MOISTURE_CRITICAL);
        assertThat(anomaly.getSeverity()).isEqualTo(AnomalySeverity.CRITICAL);
        assertThat(anomaly.getStatus()).isEqualTo(AnomalyStatus.DETECTED);
        assertThat(anomaly.getObservationCount()).isEqualTo(1);
        assertThat(anomaly.getDetectedValue()).isEqualByComparingTo(BigDecimal.valueOf(12.5));
        assertThat(anomaly.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.95));
        assertThat(anomaly.isSimulated()).isFalse();
        assertThat(anomaly.getEvidence()).hasSize(1);
    }

    @Test
    @DisplayName("Lifecycle transitions: DETECTED -> ACKNOWLEDGED -> INVESTIGATING -> RESOLVED")
    void shouldFollowValidLifecycleTransitions() {
        UUID fieldId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AnomalyCandidate candidate = new AnomalyCandidate(
                AnomalyType.WATER_FLOW_SPIKE,
                AnomalySeverity.HIGH,
                BigDecimal.valueOf(45.0),
                BigDecimal.valueOf(20.0),
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(0.85),
                "Unexpected flow spike",
                "Flow exceeded upper tolerance",
                "RULE_ENGINE",
                List.of(),
                false
        );

        Anomaly anomaly = Anomaly.create(fieldId, null, SensorType.WATER_FLOW, candidate, Instant.now());
        assertThat(anomaly.getStatus()).isEqualTo(AnomalyStatus.DETECTED);

        // Acknowledge
        anomaly.acknowledge(userId, Instant.now());
        assertThat(anomaly.getStatus()).isEqualTo(AnomalyStatus.ACKNOWLEDGED);
        assertThat(anomaly.getAcknowledgedBy()).isEqualTo(userId);
        assertThat(anomaly.getAcknowledgedAt()).isNotNull();

        // Investigate
        anomaly.investigate(userId, Instant.now());
        assertThat(anomaly.getStatus()).isEqualTo(AnomalyStatus.INVESTIGATING);

        // Resolve
        anomaly.resolve(userId, "Gate valve calibrated successfully", Instant.now());
        assertThat(anomaly.getStatus()).isEqualTo(AnomalyStatus.RESOLVED);
        assertThat(anomaly.getResolvedBy()).isEqualTo(userId);
        assertThat(anomaly.getResolutionNotes()).isEqualTo("Gate valve calibrated successfully");
        assertThat(anomaly.getStatus().isTerminal()).isTrue();
    }

    @Test
    @DisplayName("Terminal states prevent further state transitions")
    void shouldPreventTransitionsFromTerminalState() {
        UUID fieldId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AnomalyCandidate candidate = new AnomalyCandidate(
                AnomalyType.SENSOR_OFFLINE,
                AnomalySeverity.MEDIUM,
                null,
                null,
                null,
                BigDecimal.valueOf(0.90),
                "Heartbeat timed out",
                "Sensor has not reported",
                "SENSOR_HEALTH",
                List.of(),
                false
        );

        Anomaly anomaly = Anomaly.create(fieldId, UUID.randomUUID(), SensorType.TEMPERATURE, candidate, Instant.now());
        anomaly.dismiss(userId, "Sensor undergoing seasonal replacement", Instant.now());
        assertThat(anomaly.getStatus()).isEqualTo(AnomalyStatus.DISMISSED);

        // Cannot acknowledge or resolve dismissed anomaly
        assertThatThrownBy(() -> anomaly.acknowledge(userId, Instant.now()))
                .isInstanceOf(BusinessRuleException.class)
                .matches(e -> "INVALID_STATUS_TRANSITION".equals(((BusinessRuleException) e).getErrorCode()));

        assertThatThrownBy(() -> anomaly.resolve(userId, "Fixed", Instant.now()))
                .isInstanceOf(BusinessRuleException.class)
                .matches(e -> "INVALID_STATUS_TRANSITION".equals(((BusinessRuleException) e).getErrorCode()));
    }

    @Test
    @DisplayName("Deduplication: recordRecurrence updates lastObservedAt, observationCount, and latest value")
    void shouldRecordRecurrenceCorrectly() {
        UUID fieldId = UUID.randomUUID();
        AnomalyCandidate candidate = new AnomalyCandidate(
                AnomalyType.SOIL_MOISTURE_DROP,
                AnomalySeverity.HIGH,
                BigDecimal.valueOf(18.0),
                BigDecimal.valueOf(30.0),
                BigDecimal.valueOf(-12.0),
                BigDecimal.valueOf(0.88),
                "Rapid moisture decline",
                "Moisture dropped rapidly",
                "RULE_ENGINE",
                List.of(),
                false
        );

        Instant t0 = Instant.parse("2026-11-01T08:00:00Z");
        Anomaly anomaly = Anomaly.create(fieldId, UUID.randomUUID(), SensorType.SOIL_MOISTURE, candidate, t0);
        assertThat(anomaly.getObservationCount()).isEqualTo(1);

        Instant t1 = Instant.parse("2026-11-01T08:15:00Z");
        anomaly.recordRecurrence(
                BigDecimal.valueOf(17.2),
                "Moisture continued falling to 17.2%",
                List.of(AnomalyEvidence.of("POINT", "SOIL_MOISTURE", BigDecimal.valueOf(17.2), null, t1, null)),
                t1
        );

        assertThat(anomaly.getObservationCount()).isEqualTo(2);
        assertThat(anomaly.getLastObservedAt()).isEqualTo(t1);
        assertThat(anomaly.getDetectedValue()).isEqualByComparingTo(BigDecimal.valueOf(17.2));
        assertThat(anomaly.getExplanation()).isEqualTo("Moisture continued falling to 17.2%");
        assertThat(anomaly.getEvidence()).hasSize(1);
    }
}
