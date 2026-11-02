package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.prediction.application.dto.ActivateVersionRequest;
import com.aquaai.prediction.application.dto.ApproveProposalRequest;
import com.aquaai.prediction.application.dto.RollbackVersionRequest;
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictionGovernanceSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(98000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String farmerToken;
    private String operatorToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create Farmer
        long fId = USER_COUNTER.incrementAndGet();
        RegisterRequest regReq = RegisterRequest.builder()
                .firstName("Gov")
                .lastName("Farmer")
                .phoneNumber("+99891" + String.format("%07d", fId))
                .email("farmer." + fId + "@govsec.uz")
                .password("SecurePass123!")
                .district("Yangiariq")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class);
        farmerToken = auth.accessToken();

        // Create Operator
        long opId = USER_COUNTER.incrementAndGet();
        User opUser = userRepository.save(User.builder()
                .firstName("Gov")
                .lastName("Operator")
                .fullName("Gov Operator")
                .phoneNumber("+99893" + String.format("%07d", opId))
                .email("op." + opId + "@govsec.uz")
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
        operatorToken = jwtTokenProvider.generateAccessToken(opUser.getId(), opUser.getPhoneNumber(), opUser.getRole());
    }

    @Test
    @DisplayName("Unauthenticated requests to governance endpoints return 401 Unauthorized")
    void unauthenticatedReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/governance/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/predictions/governance/proposals/" + UUID.randomUUID() + "/approve"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/predictions/governance/versions/" + UUID.randomUUID() + "/activate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer role cannot approve proposals or activate/rollback versions (403 Forbidden)")
    void farmerCannotPerformAdminGovernanceActions() throws Exception {
        UUID proposalId = UUID.randomUUID();
        ApproveProposalRequest approveReq = new ApproveProposalRequest("Attempted unauthorized approval");

        mockMvc.perform(post("/api/v1/predictions/governance/proposals/" + proposalId + "/approve")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isForbidden());

        ActivateVersionRequest activateReq = new ActivateVersionRequest("ACTIVE", "Attempted activation");
        mockMvc.perform(post("/api/v1/predictions/governance/versions/" + UUID.randomUUID() + "/activate")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateReq)))
                .andExpect(status().isForbidden());

        RollbackVersionRequest rollbackReq = new RollbackVersionRequest("V1", "Attempted rollback");
        mockMvc.perform(post("/api/v1/predictions/governance/versions/rollback")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("type", "SOIL_MOISTURE_FORECAST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rollbackReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operator role cannot approve proposals or activate/rollback versions (403 Forbidden)")
    void operatorCannotPerformAdminGovernanceActions() throws Exception {
        UUID proposalId = UUID.randomUUID();
        ApproveProposalRequest approveReq = new ApproveProposalRequest("Attempted operator approval");

        mockMvc.perform(post("/api/v1/predictions/governance/proposals/" + proposalId + "/approve")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isForbidden());

        ActivateVersionRequest activateReq = new ActivateVersionRequest("ACTIVE", "Attempted operator activation");
        mockMvc.perform(post("/api/v1/predictions/governance/versions/" + UUID.randomUUID() + "/activate")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateReq)))
                .andExpect(status().isForbidden());
    }
}
