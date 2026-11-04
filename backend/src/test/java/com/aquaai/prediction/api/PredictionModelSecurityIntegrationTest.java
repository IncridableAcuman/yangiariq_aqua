package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.prediction.application.dto.PromoteCandidateRequest;
import com.aquaai.prediction.application.dto.TrainPredictionModelRequest;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictionModelSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(103000);

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
    void setUp() {
        long fId = USER_COUNTER.incrementAndGet();
        User farmerUser = userRepository.save(User.builder()
                .firstName("Sec")
                .lastName("Farmer")
                .fullName("Sec Farmer")
                .phoneNumber("+99890" + fId)
                .email("secfarmer." + fId + "@yangiariq.uz")
                .passwordHash("hashed")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
        farmerToken = "Bearer " + jwtTokenProvider.generateAccessToken(farmerUser.getId(), farmerUser.getPhoneNumber(), farmerUser.getRole());

        long opId = USER_COUNTER.incrementAndGet();
        User opUser = userRepository.save(User.builder()
                .firstName("Sec")
                .lastName("Operator")
                .fullName("Sec Operator")
                .phoneNumber("+99890" + opId)
                .email("secop." + opId + "@yangiariq.uz")
                .passwordHash("$2a$10$validPasswordHashForSecOperator123")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
        operatorToken = "Bearer " + jwtTokenProvider.generateAccessToken(opUser.getId(), opUser.getPhoneNumber(), opUser.getRole());
    }

    @Test
    @DisplayName("Unauthenticated requests return 401 Unauthorized")
    void unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/models/candidates"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/predictions/models/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("FARMER role cannot initiate model training (403 Forbidden)")
    void farmerCannotTrainModel() throws Exception {
        TrainPredictionModelRequest request = new TrainPredictionModelRequest(
                PredictionType.SOIL_MOISTURE_FORECAST,
                null, Instant.now().minusSeconds(86400), Instant.now(), 1.0
        );

        mockMvc.perform(post("/api/v1/predictions/models/train")
                        .header("Authorization", farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OPERATOR role cannot promote or activate model candidates (403 Forbidden)")
    void operatorCannotPromoteModel() throws Exception {
        PromoteCandidateRequest request = new PromoteCandidateRequest(MethodVersionStatus.APPROVED, "Attempt by operator");

        mockMvc.perform(post("/api/v1/predictions/models/candidates/" + UUID.randomUUID() + "/promote")
                        .header("Authorization", operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
