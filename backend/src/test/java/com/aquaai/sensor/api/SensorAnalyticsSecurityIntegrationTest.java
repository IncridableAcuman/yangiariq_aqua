package com.aquaai.sensor.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.sensor.api.dto.CreateSensorRequest;
import com.aquaai.sensor.api.dto.RecordSensorReadingRequest;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SensorAnalyticsSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(9500);

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
        String phone = "+99890" + String.format("%07d", id);
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
                AuthResponse.class
        );
        return auth.accessToken();
    }

    private String createOperatorToken() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99891" + String.format("%07d", id);
        String email = "operator." + id + "@analytics.yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("District")
                .fullName("Operator District")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private String createAdminToken() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99892" + String.format("%07d", id);
        String email = "admin." + id + "@analytics.yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("Super")
                .fullName("Admin Super")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private UUID createField(String token, String fieldName) throws Exception {
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(60.60, 41.33),
                CoordinatePoint.of(60.61, 41.33),
                CoordinatePoint.of(60.61, 41.34),
                CoordinatePoint.of(60.60, 41.34),
                CoordinatePoint.of(60.60, 41.33)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                fieldName,
                "Sinov maydoni",
                new BigDecimal("10.50"),
                CropType.WHEAT,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                new BigDecimal("41.335000"),
                new BigDecimal("60.605000"),
                boundary
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(json.get("data").get("id").asText());
    }

    private UUID createSensor(String token, UUID fieldId, String name, SensorType type) throws Exception {
        CreateSensorRequest request = new CreateSensorRequest(
                fieldId,
                name,
                "SN-SEC-AN-" + UUID.randomUUID().toString().substring(0, 8),
                type,
                "SIMULATED",
                "Mounted at depth 30cm"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/sensors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(json.get("data").get("id").asText());
    }

    @Test
    @DisplayName("Unauthenticated request to sensor analytics returns 401 Unauthorized")
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/sensors/" + UUID.randomUUID() + "/analytics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer can view analytics for own sensor and field")
    void farmerOwnSensorAnalytics_returns200() throws Exception {
        String token = registerFarmer("Anvar", "Karimov");
        UUID fieldId = createField(token, "Anvar Bogi");
        UUID sensorId = createSensor(token, fieldId, "Moisture Alpha", SensorType.SOIL_MOISTURE);

        // Record a reading
        RecordSensorReadingRequest readingRequest = new RecordSensorReadingRequest(
                new BigDecimal("48.5"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "MANUAL",
                Instant.now().minus(10, ChronoUnit.MINUTES)
        );
        mockMvc.perform(post("/api/v1/sensors/" + sensorId + "/readings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readingRequest)))
                .andExpect(status().isCreated());

        // Sensor analytics
        mockMvc.perform(get("/api/v1/sensors/" + sensorId + "/analytics")
                        .header("Authorization", "Bearer " + token)
                        .param("interval", "1h"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // Field analytics
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/analytics")
                        .header("Authorization", "Bearer " + token)
                        .param("interval", "1h")
                        .param("sensorType", "SOIL_MOISTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Field summary
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/analytics/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.availability").exists())
                .andExpect(jsonPath("$.data.past24hDataQuality").exists());
    }

    @Test
    @DisplayName("Anti-IDOR: Farmer querying another farmer's sensor or field analytics returns 404")
    void antiIdor_farmerAccessingOtherFarmerSensor_returns404() throws Exception {
        String farmerA = registerFarmer("Farmer", "Alpha");
        UUID fieldA = createField(farmerA, "Alpha Plot");
        UUID sensorA = createSensor(farmerA, fieldA, "Alpha Probe", SensorType.SOIL_MOISTURE);

        String farmerB = registerFarmer("Farmer", "Beta");

        // Farmer B attempts to access Farmer A's sensor analytics
        mockMvc.perform(get("/api/v1/sensors/" + sensorA + "/analytics")
                        .header("Authorization", "Bearer " + farmerB))
                .andExpect(status().isNotFound());

        // Farmer B attempts to access Farmer A's field analytics
        mockMvc.perform(get("/api/v1/fields/" + fieldA + "/analytics")
                        .header("Authorization", "Bearer " + farmerB))
                .andExpect(status().isNotFound());

        // Farmer B attempts to access Farmer A's field summary
        mockMvc.perform(get("/api/v1/fields/" + fieldA + "/analytics/summary")
                        .header("Authorization", "Bearer " + farmerB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Operator and Admin have surveillance read access to all field analytics")
    void operatorAndAdmin_haveReadAccess() throws Exception {
        String farmer = registerFarmer("Qodir", "Alimov");
        UUID fieldId = createField(farmer, "Qodir Field");
        UUID sensorId = createSensor(farmer, fieldId, "Qodir Flow Meter", SensorType.WATER_FLOW);

        String operatorToken = createOperatorToken();
        String adminToken = createAdminToken();

        // Operator reads sensor analytics
        mockMvc.perform(get("/api/v1/sensors/" + sensorId + "/analytics")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        // Admin reads field analytics summary
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/analytics/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));
    }

    @Test
    @DisplayName("Invalid date range returns 422 Unprocessable Entity")
    void invalidDateRange_returnsError() throws Exception {
        String token = registerFarmer("Valid", "DateTester");
        UUID fieldId = createField(token, "Date Field");
        UUID sensorId = createSensor(token, fieldId, "Probe", SensorType.SOIL_MOISTURE);

        Instant now = Instant.now();
        Instant past = now.minus(5, ChronoUnit.DAYS);

        // from is in future relative to to
        mockMvc.perform(get("/api/v1/sensors/" + sensorId + "/analytics")
                        .header("Authorization", "Bearer " + token)
                        .param("from", now.toString())
                        .param("to", past.toString()))
                .andExpect(status().isUnprocessableEntity());
    }
}
