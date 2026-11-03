package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.prediction.application.dto.SelectAdaptiveMethodRequest;
import com.aquaai.prediction.domain.model.PredictionType;
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

import java.math.BigDecimal;
import java.util.List;
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
class PredictionAdaptiveIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(88000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String farmerToken;
    private UUID fieldId;

    @BeforeEach
    void setUp() throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String adminPhone = "+99890" + String.format("%07d", id);
        User admin = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("Chief")
                .fullName("Admin Chief")
                .phoneNumber(adminPhone)
                .email("admin." + id + "@adaptive.yangiariq.uz")
                .passwordHash("hashed")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
        adminToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getPhoneNumber(), admin.getRole());

        long fId = USER_COUNTER.incrementAndGet();
        String farmerPhone = "+99891" + String.format("%07d", fId);
        RegisterRequest regReq = RegisterRequest.builder()
                .firstName("Adaptive")
                .lastName("Farmer")
                .phoneNumber(farmerPhone)
                .email("farmer." + fId + "@adaptive.yangiariq.uz")
                .password("SecurePass123!")
                .district("Yangiariq")
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(regResult.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
        farmerToken = auth.accessToken();

        // Create a test parcel
        CreateFieldRequest fieldReq = new CreateFieldRequest(
                "Adaptive Parcel " + fId,
                "Parcel for adaptive selection",
                BigDecimal.valueOf(15.0),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                IrrigationMethod.DRIP,
                BigDecimal.valueOf(41.34),
                BigDecimal.valueOf(60.62),
                FieldBoundary.fromExteriorRing(List.of(
                        CoordinatePoint.of(60.62, 41.34),
                        CoordinatePoint.of(60.63, 41.34),
                        CoordinatePoint.of(60.63, 41.35),
                        CoordinatePoint.of(60.62, 41.35),
                        CoordinatePoint.of(60.62, 41.34)
                ))
        );

        MvcResult fieldResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fieldReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(fieldResult.getResponse().getContentAsString());
        fieldId = UUID.fromString(root.get("data").get("id").asText());
    }

    @Test
    @DisplayName("GET /api/v1/predictions/adaptive/overview should return adaptive system overview")
    void shouldReturnAdaptiveOverview() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/adaptive/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeVersionsCount").isNumber())
                .andExpect(jsonPath("$.data.contextAwareSelectionsCount").isNumber())
                .andExpect(jsonPath("$.data.contextualProfiles").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/predictions/adaptive/context/{fieldId} should return parcel context")
    void shouldReturnParcelContext() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/adaptive/context/" + fieldId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.cropType").value("COTTON"))
                .andExpect(jsonPath("$.data.irrigationMethod").value("DRIP"))
                .andExpect(jsonPath("$.data.season").exists())
                .andExpect(jsonPath("$.data.telemetryQuality").exists());
    }

    @Test
    @DisplayName("GET /api/v1/predictions/adaptive/performance should return contextual empirical profiles")
    void shouldReturnContextualPerformance() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/adaptive/performance")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("predictionType", "SOIL_MOISTURE_FORECAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/predictions/adaptive/select should execute deterministic selection and record audit decision")
    void shouldExecuteAdaptiveSelectionAndRecordDecision() throws Exception {
        SelectAdaptiveMethodRequest req = new SelectAdaptiveMethodRequest(
                fieldId,
                PredictionType.SOIL_MOISTURE_FORECAST.name()
        );

        MvcResult result = mockMvc.perform(post("/api/v1/predictions/adaptive/select")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.selectedVersionTag").exists())
                .andExpect(jsonPath("$.data.selectionMode").exists())
                .andExpect(jsonPath("$.data.selectionReason").exists())
                .andExpect(jsonPath("$.data.contextSnapshotJson").exists())
                .andReturn();

        JsonNode resData = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        String decisionId = resData.get("id").asText();
        assertThat(decisionId).isNotBlank();

        // Verify it appears in selections ledger
        mockMvc.perform(get("/api/v1/predictions/adaptive/selections")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("fieldId", fieldId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(decisionId));
    }
}
