package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.FeatureSchemaVersion;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.model.PredictionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PredictionModelCandidateUnitTest {

    @Test
    @DisplayName("Candidate transitions across governed lifecycle: CANDIDATE -> APPROVED -> SHADOW -> CANARY -> ACTIVE -> RETIRED")
    void managesGovernedLifecycleTransitions() {
        Instant now = Instant.now();
        PredictionModelCandidate candidate = PredictionModelCandidate.builder()
                .id(UUID.randomUUID())
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .modelType("REGRESSION_RIDGE_HYBRID")
                .versionTag("SOIL_MOISTURE_ML_V1")
                .datasetSnapshotId(UUID.randomUUID())
                .featureSchemaVersion(FeatureSchemaVersion.SOIL_MOISTURE_SCHEMA_V1)
                .trainingPeriodStart(now.minusSeconds(86400 * 30))
                .trainingPeriodEnd(now.minusSeconds(86400 * 10))
                .validationPeriodStart(now.minusSeconds(86400 * 10))
                .validationPeriodEnd(now.minusSeconds(86400 * 5))
                .testPeriodStart(now.minusSeconds(86400 * 5))
                .testPeriodEnd(now)
                .trainingSampleCount(60)
                .validationSampleCount(20)
                .testSampleCount(20)
                .trainingMetricsJson("{}")
                .validationMetricsJson("{}")
                .testMetricsJson("{}")
                .baselineComparisonMetricsJson("{}")
                .configurationSnapshotJson("{\"l2Penalty\":1.0,\"intercept\":25.0}")
                .artifactHash("HASH_TEST_123")
                .status(MethodVersionStatus.CANDIDATE)
                .isImmutable(false)
                .createdAt(now)
                .createdBy("ADMIN")
                .build();

        assertThat(candidate.getStatus()).isEqualTo(MethodVersionStatus.CANDIDATE);

        candidate.approve("ADMIN");
        assertThat(candidate.getStatus()).isEqualTo(MethodVersionStatus.APPROVED);

        candidate.setShadow("ADMIN");
        assertThat(candidate.getStatus()).isEqualTo(MethodVersionStatus.SHADOW);
        assertThat(candidate.isImmutable()).isTrue();

        candidate.setCanary("ADMIN");
        assertThat(candidate.getStatus()).isEqualTo(MethodVersionStatus.CANARY);

        candidate.activate("ADMIN");
        assertThat(candidate.getStatus()).isEqualTo(MethodVersionStatus.ACTIVE);

        candidate.retire("ADMIN");
        assertThat(candidate.getStatus()).isEqualTo(MethodVersionStatus.RETIRED);
    }

    @Test
    @DisplayName("Candidate creation rejects configuration attempting to bypass hydraulic safety parameters")
    void rejectsConfigurationsAttemptingSafetyBypass() {
        Instant now = Instant.now();
        assertThatThrownBy(() -> PredictionModelCandidate.builder()
                .id(UUID.randomUUID())
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .modelType("REGRESSION_RIDGE_HYBRID")
                .versionTag("SOIL_MOISTURE_ML_MALICIOUS")
                .datasetSnapshotId(UUID.randomUUID())
                .featureSchemaVersion(FeatureSchemaVersion.SOIL_MOISTURE_SCHEMA_V1)
                .trainingPeriodStart(now.minusSeconds(86400 * 30))
                .trainingPeriodEnd(now.minusSeconds(86400 * 10))
                .validationPeriodStart(now.minusSeconds(86400 * 10))
                .validationPeriodEnd(now.minusSeconds(86400 * 5))
                .testPeriodStart(now.minusSeconds(86400 * 5))
                .testPeriodEnd(now)
                .trainingSampleCount(60)
                .validationSampleCount(20)
                .testSampleCount(20)
                .trainingMetricsJson("{}")
                .validationMetricsJson("{}")
                .testMetricsJson("{}")
                .baselineComparisonMetricsJson("{}")
                .configurationSnapshotJson("{\"bypassEmergencyCutoff\": true}")
                .artifactHash("HASH_MALICIOUS")
                .status(MethodVersionStatus.CANDIDATE)
                .isImmutable(false)
                .createdAt(now)
                .createdBy("ATTACKER")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hard hydraulic safety parameters cannot be modified");
    }
}
