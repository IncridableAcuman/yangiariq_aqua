package com.aquaai.pilot.domain;

import com.aquaai.pilot.domain.model.GroundTruthMeasurement;
import com.aquaai.pilot.domain.model.GroundTruthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroundTruthFourEyesUnitTest {

    @Test
    @DisplayName("Should submit measurement in SUBMITTED status")
    void shouldSubmitMeasurement() {
        UUID pilotId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        UUID submitterId = UUID.randomUUID();

        GroundTruthMeasurement m = GroundTruthMeasurement.submit(
                pilotId,
                fieldId,
                "SOIL_MOISTURE",
                48.5,
                "%",
                Instant.now(),
                "PORTABLE_TDR_PROBE",
                "Field Inspection Team Alpha",
                submitterId,
                "CERT-TDR-2026-99",
                "Field midpoint core sample"
        );

        assertThat(m.getId()).isNotNull();
        assertThat(m.getVerificationStatus()).isEqualTo(GroundTruthStatus.SUBMITTED);
        assertThat(m.getVerifierId()).isNull();
    }

    @Test
    @DisplayName("Should enforce Four-Eyes principle: Submitter cannot verify their own measurement")
    void shouldEnforceFourEyesPrincipleOnVerify() {
        UUID pilotId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        UUID submitterId = UUID.randomUUID();

        GroundTruthMeasurement m = GroundTruthMeasurement.submit(
                pilotId,
                fieldId,
                "SOIL_MOISTURE",
                52.0,
                "%",
                Instant.now(),
                "PORTABLE_TDR_PROBE",
                "Inspector",
                submitterId,
                null,
                null
        );

        assertThatThrownBy(() -> m.verify(submitterId, "Evidence", "Self-verification attempt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Four-Eyes Principle Violation");
    }

    @Test
    @DisplayName("Should allow independent verifier to verify measurement")
    void shouldAllowIndependentVerification() {
        UUID pilotId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        UUID submitterId = UUID.randomUUID();
        UUID verifierId = UUID.randomUUID();

        GroundTruthMeasurement m = GroundTruthMeasurement.submit(
                pilotId,
                fieldId,
                "WATER_FLOW_RATE",
                12.4,
                "m3/h",
                Instant.now(),
                "ULTRASONIC_FLOW_METER",
                "Canal Turnout Inspector",
                submitterId,
                "FLOW-CALIB-881",
                "Turnout #4 gate measurement"
        );

        m.verify(verifierId, "PHOTO-REF-4401", "Calibrated ultrasonic reading checked and attested");

        assertThat(m.getVerificationStatus()).isEqualTo(GroundTruthStatus.VERIFIED);
        assertThat(m.getVerifierId()).isEqualTo(verifierId);
        assertThat(m.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should allow independent verifier to reject or request remeasurement")
    void shouldAllowRejectionAndRemeasurement() {
        UUID pilotId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        UUID submitterId = UUID.randomUUID();
        UUID verifierId = UUID.randomUUID();

        GroundTruthMeasurement m = GroundTruthMeasurement.submit(
                pilotId,
                fieldId,
                "WATER_PRESSURE",
                9.5,
                "bar",
                Instant.now(),
                "ANALOG_GAUGE",
                "Inspector",
                submitterId,
                null,
                null
        );

        m.reject(verifierId, "Gauge needle stuck or uncalibrated reading");
        assertThat(m.getVerificationStatus()).isEqualTo(GroundTruthStatus.REJECTED);
        assertThat(m.getRejectionReason()).contains("uncalibrated reading");

        m.requestRemeasurement(verifierId, "Repeat with calibrated digital manometer");
        assertThat(m.getVerificationStatus()).isEqualTo(GroundTruthStatus.NEEDS_REMEASUREMENT);
    }
}
