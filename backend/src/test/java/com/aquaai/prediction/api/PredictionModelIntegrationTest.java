package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.prediction.application.dto.PromoteCandidateRequest;
import com.aquaai.prediction.application.dto.TrainPredictionModelRequest;
import com.aquaai.prediction.domain.model.FeatureSchemaVersion;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.model.PredictionTrainingDataset;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.TrainingDatasetQuality;
import com.aquaai.prediction.domain.port.PredictionModelCandidateRepository;
import com.aquaai.prediction.domain.port.PredictionTrainingDatasetRepository;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictionModelIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(101000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PredictionModelCandidateRepository candidateRepository;

    @Autowired
    private PredictionTrainingDatasetRepository datasetRepository;

    private String adminToken;
    private String farmerToken;

    @BeforeEach
    void setUp() {
        long fId = USER_COUNTER.incrementAndGet();
        User farmerUser = userRepository.save(User.builder()
                .firstName("ML")
                .lastName("Farmer")
                .fullName("ML Farmer")
                .phoneNumber("+99890" + fId)
                .email("mlfarmer." + fId + "@yangiariq.uz")
                .passwordHash("hashed")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
        farmerToken = "Bearer " + jwtTokenProvider.generateAccessToken(farmerUser.getId(), farmerUser.getPhoneNumber(), farmerUser.getRole());

        long adminId = USER_COUNTER.incrementAndGet();
        User adminUser = userRepository.save(User.builder()
                .firstName("ML")
                .lastName("Admin")
                .fullName("ML Admin")
                .phoneNumber("+99890" + adminId)
                .email("mladmin." + adminId + "@yangiariq.uz")
                .passwordHash("hashed")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
        adminToken = "Bearer " + jwtTokenProvider.generateAccessToken(adminUser.getId(), adminUser.getPhoneNumber(), adminUser.getRole());
    }

    @Test
    @DisplayName("GET /api/v1/predictions/models/comparison returns active strategy and baseline metrics")
    void getActiveComparisonReturnsValidStrategy() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/models/comparison")
                        .header("Authorization", farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.predictionType").value("SOIL_MOISTURE_FORECAST"))
                .andExpect(jsonPath("$.data.baselineModelVersion").value("SOIL_MOISTURE_TREND_V1"))
                .andExpect(jsonPath("$.data.contextHealth").value("HEALTHY"));
    }

    @Test
    @DisplayName("POST /api/v1/predictions/models/train returns INSUFFICIENT_TRAINING_DATA when verified samples are below gate")
    void trainAbortsHonestlyWhenDataInsufficient() throws Exception {
        TrainPredictionModelRequest request = new TrainPredictionModelRequest(
                PredictionType.SOIL_MOISTURE_FORECAST,
                null,
                Instant.now().minusSeconds(86400 * 30),
                Instant.now(),
                1.0
        );

        mockMvc.perform(post("/api/v1/predictions/models/train")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INSUFFICIENT_TRAINING_DATA"))
                .andExpect(jsonPath("$.data.minRequiredSamples").value(30))
                .andExpect(jsonPath("$.data.baselineComparisonSummary").value(org.hamcrest.Matchers.containsString("Deterministic baseline remains authoritative")));
    }

    @Test
    @DisplayName("Governed candidate lifecycle: retrieval, detail inspection, and promotion")
    void candidateLifecycleAndPromotion() throws Exception {
        Instant now = Instant.now();

        // 1. Create dataset snapshot
        PredictionTrainingDataset ds = datasetRepository.save(PredictionTrainingDataset.create(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "HASH_TEST_SNAPSHOT_0811",
                FeatureSchemaVersion.SOIL_MOISTURE_SCHEMA_V1,
                now.minusSeconds(86400 * 30),
                now,
                "ALL",
                80,
                new TrainingDatasetQuality(80, 80, 0, 0, 0, 0, 0, TrainingDatasetQuality.DataQualityStatus.HIGH_QUALITY, java.util.List.of()),
                "{\"validRecords\":80}",
                "ADMIN"
        ));

        // 2. Create candidate
        String vTag = "SOIL_MOISTURE_ML_TEST_" + UUID.randomUUID().toString().substring(0, 8);
        PredictionModelCandidate candidate = candidateRepository.save(PredictionModelCandidate.builder()
                .id(UUID.randomUUID())
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .modelType("REGRESSION_RIDGE_HYBRID")
                .versionTag(vTag)
                .datasetSnapshotId(ds.getId())
                .featureSchemaVersion(FeatureSchemaVersion.SOIL_MOISTURE_SCHEMA_V1)
                .trainingPeriodStart(now.minusSeconds(86400 * 30))
                .trainingPeriodEnd(now.minusSeconds(86400 * 10))
                .validationPeriodStart(now.minusSeconds(86400 * 10))
                .validationPeriodEnd(now.minusSeconds(86400 * 5))
                .testPeriodStart(now.minusSeconds(86400 * 5))
                .testPeriodEnd(now)
                .trainingSampleCount(50)
                .validationSampleCount(15)
                .testSampleCount(15)
                .trainingMetricsJson("{\"mae\":4.8,\"rmse\":5.9}")
                .validationMetricsJson("{\"mae\":5.1,\"rmse\":6.2}")
                .testMetricsJson("{\"mae\":5.2,\"rmse\":6.3}")
                .baselineComparisonMetricsJson("{\"mlMae\":5.2,\"baselineMae\":6.8,\"maeImprovementPercent\":23.5,\"regressionDetected\":false,\"recommendationStatus\":\"RECOMMENDED\"}")
                .configurationSnapshotJson("{\"modelAlgorithm\":\"RIDGE_REGRESSION\",\"weights\":[0.1,0.2],\"intercept\":24.5,\"featureImportance\":{\"soil_moisture_current\":0.82}}")
                .artifactHash("HASH_ARTIFACT_TEST_456")
                .status(MethodVersionStatus.CANDIDATE)
                .isImmutable(false)
                .createdAt(now)
                .createdBy("ADMIN")
                .build());

        // 3. List candidates
        mockMvc.perform(get("/api/v1/predictions/models/candidates")
                        .header("Authorization", farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 4. Candidate detail
        mockMvc.perform(get("/api/v1/predictions/models/candidates/" + candidate.getId())
                        .header("Authorization", farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versionTag").value(vTag))
                .andExpect(jsonPath("$.data.featureImportance.soil_moisture_current").value(0.82));

        // 5. Promote candidate: CANDIDATE -> APPROVED
        PromoteCandidateRequest promoteReq = new PromoteCandidateRequest(MethodVersionStatus.APPROVED, "Peer review complete.");
        mockMvc.perform(post("/api/v1/predictions/models/candidates/" + candidate.getId() + "/promote")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoteReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // 6. Promote candidate: APPROVED -> SHADOW
        PromoteCandidateRequest shadowReq = new PromoteCandidateRequest(MethodVersionStatus.SHADOW, "Starting shadow rollout.");
        mockMvc.perform(post("/api/v1/predictions/models/candidates/" + candidate.getId() + "/promote")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shadowReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHADOW"));

        // 7. Verify 404 on non-existent candidate (anti-IDOR)
        mockMvc.perform(get("/api/v1/predictions/models/candidates/" + UUID.randomUUID())
                        .header("Authorization", farmerToken))
                .andExpect(status().isNotFound());
    }
}
