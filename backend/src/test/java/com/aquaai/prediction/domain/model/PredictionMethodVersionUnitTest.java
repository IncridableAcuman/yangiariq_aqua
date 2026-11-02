package com.aquaai.prediction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PredictionMethodVersion Domain Model Unit Tests")
class PredictionMethodVersionUnitTest {

    @Test
    @DisplayName("Should create candidate version successfully with valid soft parameters")
    void shouldCreateCandidateVersion() {
        PredictionMethodVersion version = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V2",
                "Calibrated smoothing alpha and Kc",
                "{\"smoothingAlpha\":0.25,\"trendWindowHours\":36,\"kcReference\":1.05}",
                "ADMIN_USER"
        );

        assertThat(version.getId()).isNotNull();
        assertThat(version.getVersionTag()).isEqualTo("SOIL_MOISTURE_TREND_V2");
        assertThat(version.getStatus()).isEqualTo(MethodVersionStatus.CANDIDATE);
        assertThat(version.isImmutable()).isFalse();
        assertThat(version.getCreatedBy()).isEqualTo("ADMIN_USER");
    }

    @Test
    @DisplayName("Should reject version configuration attempting to modify hard hydraulic safety interlocks")
    void shouldRejectHardSafetyConstraintsModification() {
        assertThatThrownBy(() -> PredictionMethodVersion.createCandidate(
                PredictionType.IRRIGATION_NEED_FORECAST,
                "ILLEGAL_SAFETY_OVERRIDE_V1",
                "Attempts to bypass hydraulic safety interlock",
                "{\"madThresholdFraction\":0.40,\"hydraulicSafetyInterlock\":false}",
                "MALICIOUS_ACTOR"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hard hydraulic safety parameters cannot be modified");

        assertThatThrownBy(() -> PredictionMethodVersion.createCandidate(
                PredictionType.WATER_DEMAND_FORECAST,
                "ILLEGAL_BURST_OVERRIDE_V1",
                "Attempts to modify pipe burst threshold",
                "{\"applicationEfficiency\":0.85,\"pipeBurstPressure\":8.0}",
                "MALICIOUS_ACTOR"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hard hydraulic safety parameters cannot be modified");
    }

    @Test
    @DisplayName("Should prevent modifying configuration once marked immutable")
    void shouldEnforceImmutabilityOnceLocked() {
        PredictionMethodVersion version = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V3",
                "Draft candidate",
                "{\"smoothingAlpha\":0.35}",
                "ADMIN_USER"
        );

        version.markImmutable();
        assertThat(version.isImmutable()).isTrue();

        assertThatThrownBy(() -> version.updateConfiguration("{\"smoothingAlpha\":0.40}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify immutable prediction method version");
    }

    @Test
    @DisplayName("Should transition through lifecycle states: CANDIDATE -> APPROVED -> SHADOW -> CANARY -> ACTIVE -> DEPRECATED")
    void shouldTransitionThroughLifecycle() {
        PredictionMethodVersion version = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V4",
                "Lifecycle test",
                "{\"smoothingAlpha\":0.28}",
                "ADMIN_USER"
        );

        version.approve("REVIEWER");
        assertThat(version.getStatus()).isEqualTo(MethodVersionStatus.APPROVED);

        version.setShadow("REVIEWER");
        assertThat(version.getStatus()).isEqualTo(MethodVersionStatus.SHADOW);

        version.setCanary("REVIEWER");
        assertThat(version.getStatus()).isEqualTo(MethodVersionStatus.CANARY);

        version.activate("OPERATOR_LEAD");
        assertThat(version.getStatus()).isEqualTo(MethodVersionStatus.ACTIVE);
        assertThat(version.getActivatedAt()).isNotNull();
        assertThat(version.getActivatedBy()).isEqualTo("OPERATOR_LEAD");
        assertThat(version.isImmutable()).isTrue();

        version.deprecate();
        assertThat(version.getStatus()).isEqualTo(MethodVersionStatus.DEPRECATED);
        assertThat(version.getRetiredAt()).isNotNull();
    }
}
