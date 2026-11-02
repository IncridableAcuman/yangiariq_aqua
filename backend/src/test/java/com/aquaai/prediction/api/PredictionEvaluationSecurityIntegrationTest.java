package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictionEvaluationSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(90000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PredictionRepository predictionRepository;

    private String registerFarmer(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99895" + String.format("%07d", id);
        String email = "farmer." + id + "@evalsec.yangiariq.uz";

        RegisterRequest request = RegisterRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phone)
                .email(email)
                .password("SecurePass123!")
                .district("Yangiariq")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class);
        return auth.accessToken();
    }

    private String createOperatorToken() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99897" + String.format("%07d", id);
        String email = "op." + id + "@evalsec.yangiariq.uz";

        User operator = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("Khorazm")
                .fullName("Operator Khorazm")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(operator.getId(), operator.getPhoneNumber(), operator.getRole());
    }

    private UUID createField(String token, String name) throws Exception {
        double lat = 41.35;
        double lon = 60.60;
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(lon, lat),
                CoordinatePoint.of(lon + 0.01, lat),
                CoordinatePoint.of(lon + 0.01, lat + 0.01),
                CoordinatePoint.of(lon, lat + 0.01),
                CoordinatePoint.of(lon, lat)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                name,
                "Evaluation security parcel " + name,
                BigDecimal.valueOf(8.5),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                new BigDecimal(String.valueOf(lat)),
                new BigDecimal(String.valueOf(lon)),
                boundary
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(root.get("data").get("id").asText());
    }

    @Test
    @DisplayName("Anti-IDOR: Farmer querying foreign prediction detail receives 404 NOT FOUND")
    void farmerCannotAccessForeignPredictionDetail() throws Exception {
        String tokenA = registerFarmer("FarmerA", "Aliyev");
        UUID fieldA = createField(tokenA, "Field A");

        String tokenB = registerFarmer("FarmerB", "Valiyev");

        UUID predA = UUID.randomUUID();
        Prediction pred = Prediction.builder()
                .id(predA)
                .fieldId(fieldA)
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .targetTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .generatedAt(Instant.now().minus(26, ChronoUnit.HOURS))
                .predictedValue(new BigDecimal("24.00"))
                .confidencePercent(80)
                .riskLevel(RiskLevel.LOW)
                .build();
        predictionRepository.save(pred);

        // Farmer B attempts to access prediction belonging to Farmer A's field
        mockMvc.perform(get("/api/v1/predictions/" + predA + "/evaluation")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Anti-IDOR: Farmer querying history for non-owned field receives 404 NOT FOUND")
    void farmerCannotFilterByForeignField() throws Exception {
        String tokenA = registerFarmer("FarmerC", "Ganiev");
        UUID fieldA = createField(tokenA, "Field C");

        String tokenB = registerFarmer("FarmerD", "Tursunov");

        mockMvc.perform(get("/api/v1/predictions/evaluation/history")
                        .header("Authorization", "Bearer " + tokenB)
                        .param("fieldId", fieldA.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Operator has broad access to query evaluation across district fields")
    void operatorHasBroadAccess() throws Exception {
        String tokenA = registerFarmer("FarmerE", "Bekov");
        UUID fieldA = createField(tokenA, "Field E");

        String opToken = createOperatorToken();

        mockMvc.perform(get("/api/v1/predictions/evaluation/history")
                        .header("Authorization", "Bearer " + opToken)
                        .param("fieldId", fieldA.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated request to evaluation overview returns 401 UNAUTHORIZED")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/evaluation/overview"))
                .andExpect(status().isUnauthorized());
    }
}
