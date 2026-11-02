package com.aquaai.analytics.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmPerformanceAnalyticsIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(50000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String registerFarmer(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99895" + String.format("%07d", id);
        String email = "farmer." + id + "@analytics.yangiariq.uz";

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
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
                AuthResponse.class);
        return auth.accessToken();
    }

    private String createOperatorToken() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99897" + String.format("%07d", id);
        String email = "op." + id + "@operator.yangiariq.uz";

        User operator = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("Yangiariq")
                .fullName("Operator Yangiariq")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(operator.getId(), operator.getPhoneNumber(), operator.getRole());
    }

    private UUID createField(String token, String name, CropType cropType) throws Exception {
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
                "Analytics parcel " + name,
                BigDecimal.valueOf(10.5),
                cropType,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
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
    @DisplayName("Farmer can retrieve farm performance analytics overview with tenant isolation")
    void testFarmerOverviewEndpoint() throws Exception {
        String tokenA = registerFarmer("Anvar", "Sobirov");
        createField(tokenA, "Anvar Dala 1", CropType.COTTON);

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("period", "LAST_30_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.period.type").value("LAST_30_DAYS"))
                .andExpect(jsonPath("$.data.period.label").isNotEmpty())
                .andExpect(jsonPath("$.data.farmOverview.totalFields").value(1))
                .andExpect(jsonPath("$.data.dataConfidence.overall").isNotEmpty());
    }

    @Test
    @DisplayName("Anti-IDOR: Farmer querying overview for non-owned field receives 404 NOT FOUND")
    void testAntiIdorProtectionOnFieldOverview() throws Exception {
        String tokenA = registerFarmer("Bobur", "Karimov");
        createField(tokenA, "Bobur Dala 1", CropType.COTTON);

        String tokenB = registerFarmer("Dilshod", "Rahimov");
        UUID fieldB = createField(tokenB, "Dilshod Dala 1", CropType.WHEAT);

        // Bobur attempts to access Dilshod's field in analytics
        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("fieldId", fieldB.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Retrieve performance trends endpoint")
    void testPerformanceTrendsEndpoint() throws Exception {
        String token = registerFarmer("Eldor", "Jalilov");
        createField(token, "Eldor Paxta Dala", CropType.COTTON);

        mockMvc.perform(get("/api/v1/analytics/trends")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "LAST_30_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.granularity").isNotEmpty())
                .andExpect(jsonPath("$.data.trendPoints").isArray());
    }

    @Test
    @DisplayName("Retrieve ranked fields endpoint")
    void testFieldRankingsEndpoint() throws Exception {
        String token = registerFarmer("Farhod", "Qodirov");
        createField(token, "Farhod Bogʻ 1", CropType.MELON);
        createField(token, "Farhod Dala 2", CropType.COTTON);

        mockMvc.perform(get("/api/v1/analytics/fields")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "LAST_30_DAYS")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "score")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fields").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("Retrieve actionable insights endpoint")
    void testFarmInsightsEndpoint() throws Exception {
        String token = registerFarmer("Gʻayrat", "Alimov");
        createField(token, "Gʻayrat Bugʻdoy", CropType.WHEAT);

        mockMvc.perform(get("/api/v1/analytics/insights")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "LAST_30_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Operator can query district-wide performance trends")
    void testOperatorAccess() throws Exception {
        String opToken = createOperatorToken();

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + opToken)
                        .param("period", "LAST_7_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Retrieve dedicated water analytics sub-endpoint")
    void testWaterSubEndpoint() throws Exception {
        String token = registerFarmer("Hamid", "Yusupov");
        createField(token, "Hamid Dala", CropType.COTTON);

        mockMvc.perform(get("/api/v1/analytics/water")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "LAST_30_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.utilizationPercent").exists())
                .andExpect(jsonPath("$.data.utilizationStatus").exists());
    }

    @Test
    @DisplayName("Retrieve dedicated irrigation analytics sub-endpoint")
    void testIrrigationSubEndpoint() throws Exception {
        String token = registerFarmer("Ilhom", "Nosirov");
        createField(token, "Ilhom Dala", CropType.WHEAT);

        mockMvc.perform(get("/api/v1/analytics/irrigation")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "LAST_30_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPlans").exists())
                .andExpect(jsonPath("$.data.planAdherencePercent").exists());
    }

    @Test
    @DisplayName("Retrieve dedicated telemetry analytics sub-endpoint")
    void testTelemetrySubEndpoint() throws Exception {
        String token = registerFarmer("Javohir", "Karimov");
        createField(token, "Javohir Dala", CropType.RICE);

        mockMvc.perform(get("/api/v1/analytics/telemetry")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "LAST_30_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.telemetryCoveragePercent").exists());
    }

    @Test
    @DisplayName("Retrieve dedicated verification analytics sub-endpoint")
    void testVerificationSubEndpoint() throws Exception {
        String token = registerFarmer("Komil", "Ergashev");
        createField(token, "Komil Dala", CropType.MAIZE);

        mockMvc.perform(get("/api/v1/analytics/verification")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "LAST_30_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationCoveragePercent").exists());
    }

    @Test
    @DisplayName("Custom period exceeding 365 days returns business rule error")
    void testCustomPeriodValidation() throws Exception {
        String token = registerFarmer("Laziz", "Shokirov");

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "CUSTOM")
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2026-01-01T00:00:00Z"))
                .andExpect(status().isUnprocessableEntity());
    }
}
