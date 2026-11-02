package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.prediction.application.dto.ActivateVersionRequest;
import com.aquaai.prediction.application.dto.ApproveProposalRequest;
import com.aquaai.prediction.application.dto.RollbackVersionRequest;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceRiskLevel;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.fasterxml.jackson.databind.JsonNode;
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
class PredictionGovernanceIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(95000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PredictionMethodVersionRepository versionRepository;

    @Autowired
    private CalibrationProposalRepository proposalRepository;

    private String adminToken;

    @BeforeEach
    void setUp() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "admin." + id + "@gov.yangiariq.uz";

        User adminUser = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("Director")
                .fullName("Admin Director")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        adminToken = jwtTokenProvider.generateAccessToken(adminUser.getId(), adminUser.getPhoneNumber(), adminUser.getRole());
    }

    @Test
    @DisplayName("GET /api/v1/predictions/governance/overview should return system governance overview")
    void shouldReturnGovernanceOverview() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/governance/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeVersionsCount").isNumber())
                .andExpect(jsonPath("$.data.activeVersions").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/predictions/governance/versions should return seeded versions")
    void shouldReturnBaselineMethodVersions() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/governance/versions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("type", "SOIL_MOISTURE_FORECAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].versionTag").exists());
    }

    @Test
    @DisplayName("End-to-End Governance Cycle: Backtest -> Approve -> Activate Candidate -> Rollback")
    void shouldExecuteFullGovernanceLifecycle() throws Exception {
        // 1. Create candidate version and calibration proposal
        String candidateTag = "SOIL_MOISTURE_TREND_V" + System.currentTimeMillis();
        PredictionMethodVersion candidateVersion = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                candidateTag,
                "Candidate model with tuned 0.28 alpha",
                "{\"smoothingAlpha\":0.28,\"trendWindowHours\":24}",
                "SYSTEM"
        );
        versionRepository.save(candidateVersion);

        CalibrationProposal proposal = CalibrationProposal.create(
                "PROP-" + System.currentTimeMillis() % 10000,
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                candidateTag,
                "Persistent overprediction observed in mid-season loam parcels",
                "Mean Bias Error = +6.4%",
                "Lower smoothing alpha to 0.28 and reduce responsiveness to sudden spikes",
                "{\"smoothingAlpha\":0.28,\"trendWindowHours\":24}",
                "Expected 18% bias reduction without increasing MAE",
                GovernanceRiskLevel.LOW,
                "SYSTEM"
        );
        proposalRepository.save(proposal);

        // 2. Trigger backtest
        MvcResult backtestResult = mockMvc.perform(post("/api/v1/predictions/governance/proposals/" + proposal.getId() + "/backtest")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.candidateVersionTag").value(candidateTag))
                .andReturn();

        // 3. Admin approves proposal
        ApproveProposalRequest approveReq = new ApproveProposalRequest(
                "Approved after reviewing anti-leakage backtest evaluation."
        );
        mockMvc.perform(post("/api/v1/predictions/governance/proposals/" + proposal.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // 4. Admin activates candidate version in production
        ActivateVersionRequest activateReq = new ActivateVersionRequest("ACTIVE", "Production rollout authorized.");
        mockMvc.perform(post("/api/v1/predictions/governance/versions/" + candidateVersion.getId() + "/activate")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.isImmutable").value(true));

        // 5. Admin executes 1-click rollback back to V1
        RollbackVersionRequest rollbackReq = new RollbackVersionRequest(
                "SOIL_MOISTURE_TREND_V1",
                "Rollback initiated due to unexpected validation drift in field 2."
        );
        mockMvc.perform(post("/api/v1/predictions/governance/versions/rollback")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("type", "SOIL_MOISTURE_FORECAST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rollbackReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versionTag").value("SOIL_MOISTURE_TREND_V1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // 6. Verify Governance Audit Trail contains full history
        mockMvc.perform(get("/api/v1/predictions/governance/audit-trail")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.action == 'VERSION_ACTIVATED')]").exists())
                .andExpect(jsonPath("$.data[?(@.action == 'VERSION_ROLLBACK')]").exists());
    }
}
