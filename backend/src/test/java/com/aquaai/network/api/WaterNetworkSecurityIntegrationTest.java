package com.aquaai.network.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.network.api.dto.ApproveDispatchRequest;
import com.aquaai.network.api.dto.DispatchOptimizationRequest;
import com.aquaai.network.api.dto.RecordActualDeliveryRequest;
import com.aquaai.network.api.dto.ResilienceScenarioRequest;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;
import com.aquaai.network.domain.model.FairnessPolicy;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WaterNetworkSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 7500000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WaterNetworkNodeRepository nodeRepository;

    @Autowired
    private WaterNetworkEdgeRepository edgeRepository;

    @Autowired
    private DispatchScheduleRepository dispatchScheduleRepository;

    private String registerFarmer() throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "network.farmer." + id + "." + UUID.randomUUID().toString().substring(0, 5) + "@yangiariq.uz";

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Network")
                .lastName("Farmer")
                .phoneNumber(phone)
                .email(email)
                .password("SecurePass123!")
                .district("Yangiariq")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
        return auth.accessToken();
    }

    private String createOperatorToken() {
        long id = USER_COUNTER.incrementAndGet();
        User op = userRepository.save(User.builder()
                .firstName("Network")
                .lastName("Operator")
                .fullName("Network District Operator")
                .phoneNumber("+99893" + String.format("%07d", id))
                .email("network.operator." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForOperator123456789")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(op.getId(), op.getEmail(), UserRole.OPERATOR);
    }

    @Test
    @DisplayName("Unauthenticated request to network overview fails with 401 or 403")
    void unauthenticatedRequestFails() throws Exception {
        mockMvc.perform(get("/api/v1/water-network/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated user can view network overview and topology")
    void authenticatedUserCanViewOverviewAndTopology() throws Exception {
        String token = registerFarmer();

        mockMvc.perform(get("/api/v1/water-network/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalNodes").isNumber())
                .andExpect(jsonPath("$.data.totalEdges").isNumber());

        mockMvc.perform(get("/api/v1/water-network/topology")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nodes").isArray())
                .andExpect(jsonPath("$.data.edges").isArray());
    }

    @Test
    @DisplayName("Farmer cannot trigger canal dispatch optimization (403 Forbidden)")
    void farmerCannotTriggerDispatchOptimization() throws Exception {
        String token = registerFarmer();

        DispatchOptimizationRequest req = new DispatchOptimizationRequest(
                null,
                Instant.now(),
                Instant.now().plusSeconds(86400 * 7),
                "CROP_CRITICALITY_FIRST",
                "Unauthorized farmer attempt"
        );

        mockMvc.perform(post("/api/v1/water-network/dispatch/optimize")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operator can trigger dispatch optimization and approve the proposal")
    void operatorCanOptimizeAndApproveSchedule() throws Exception {
        String operatorToken = createOperatorToken();

        DispatchOptimizationRequest req = new DispatchOptimizationRequest(
                null,
                Instant.now(),
                Instant.now().plusSeconds(86400 * 7),
                "CROP_CRITICALITY_FIRST",
                "Automated test proposal"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/water-network/dispatch/optimize")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value(anyOf(is("PROPOSED"), is("REVIEW_REQUIRED"))))
                .andReturn();

        String scheduleId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // Operator approves schedule
        ApproveDispatchRequest approveReq = new ApproveDispatchRequest("Approved by integration test");
        mockMvc.perform(post("/api/v1/water-network/dispatch/schedules/" + scheduleId + "/approve")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("Operator can simulate isolated resilience scenario")
    void operatorCanSimulateResilienceScenario() throws Exception {
        String operatorToken = createOperatorToken();

        ResilienceScenarioRequest req = new ResilienceScenarioRequest(
                "Secondary Canal Rupture Simulation",
                "CANAL_OUTAGE",
                Collections.emptyList(),
                Collections.emptyList(),
                new BigDecimal("25.00"),
                BigDecimal.ZERO
        );

        mockMvc.perform(post("/api/v1/water-network/scenarios/simulate")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Secondary Canal Rupture Simulation"))
                .andExpect(jsonPath("$.data.scenarioType").value("CANAL_OUTAGE"));
    }
}
