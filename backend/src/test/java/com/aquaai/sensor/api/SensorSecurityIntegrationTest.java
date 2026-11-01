package com.aquaai.sensor.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.sensor.api.dto.CreateSensorRequest;
import com.aquaai.sensor.api.dto.RecordSensorReadingRequest;
import com.aquaai.sensor.api.dto.UpdateSensorRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SensorSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(9000);

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
        String email = "farmer." + id + "@sensor.yangiariq.uz";

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
        String email = "operator." + id + "@sensor.yangiariq.uz";

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
        String email = "admin." + id + "@sensor.yangiariq.uz";

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
                CoordinatePoint.of(60.62, 41.33),
                CoordinatePoint.of(60.62, 41.35),
                CoordinatePoint.of(60.60, 41.35),
                CoordinatePoint.of(60.60, 41.33)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                fieldName,
                "Sensor sinov maydoni",
                new BigDecimal("10.00"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.CANAL,
                new BigDecimal("41.340000"),
                new BigDecimal("60.610000"),
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

    private UUID createSensor(String token, UUID fieldId, String deviceId, SensorType type) throws Exception {
        CreateSensorRequest request = new CreateSensorRequest(
                fieldId,
                "Sensor " + deviceId,
                deviceId,
                type,
                "SIMULATED",
                "Placement at root zone"
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
    @DisplayName("Unauthenticated requests to sensor endpoints return 401 Unauthorized")
    void unauthenticatedRequests_return401() throws Exception {
        mockMvc.perform(get("/api/v1/sensors/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer can register, inspect, and manage sensors on own field parcel (200 / 201)")
    void farmerOwnSensor_accessAllowed_success() throws Exception {
        String farmerToken = registerFarmer("Anvar", "Qodirov");
        UUID fieldId = createField(farmerToken, "Anvar Paxtazor");

        String deviceId = "SN-OWN-" + UUID.randomUUID().toString().substring(0, 8);
        UUID sensorId = createSensor(farmerToken, fieldId, deviceId, SensorType.SOIL_MOISTURE);

        // Get Sensor
        mockMvc.perform(get("/api/v1/sensors/" + sensorId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sensorId.toString()))
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.deviceIdentifier").value(deviceId))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Deactivate Sensor
        mockMvc.perform(patch("/api/v1/sensors/" + sensorId + "/deactivate")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        // Reactivate Sensor
        mockMvc.perform(patch("/api/v1/sensors/" + sensorId + "/activate")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Farmer attempting to access or mutate another farmer's sensor receives 404 Not Found (Anti-IDOR)")
    void farmerAnotherFieldSensor_antiIdor_returns404() throws Exception {
        String farmerAToken = registerFarmer("FarmerA", "Aliyev");
        String farmerBToken = registerFarmer("FarmerB", "Boboyev");

        UUID fieldA = createField(farmerAToken, "Field A");
        String deviceId = "SN-A-" + UUID.randomUUID().toString().substring(0, 8);
        UUID sensorA = createSensor(farmerAToken, fieldA, deviceId, SensorType.SOIL_MOISTURE);

        // Farmer B gets Farmer A's sensor -> 404 (prevents enumeration)
        mockMvc.perform(get("/api/v1/sensors/" + sensorA)
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound());

        // Farmer B attempts to update Farmer A's sensor -> 404
        UpdateSensorRequest updateReq = new UpdateSensorRequest("Hacked Name", "Malicious desc");
        mockMvc.perform(patch("/api/v1/sensors/" + sensorA)
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // Farmer B attempts to list sensors on Farmer A's field -> 404
        mockMvc.perform(get("/api/v1/fields/" + fieldA + "/sensors")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Operator has read-only district surveillance: can read sensors (200), but cannot mutate (403)")
    void operator_readAllowed_mutationForbidden() throws Exception {
        String farmerToken = registerFarmer("Dilshod", "Ergashev");
        String operatorToken = createOperatorToken();

        UUID fieldId = createField(farmerToken, "Dilshod Dalasi");
        String deviceId = "SN-OP-" + UUID.randomUUID().toString().substring(0, 8);
        UUID sensorId = createSensor(farmerToken, fieldId, deviceId, SensorType.WATER_FLOW);

        // Operator reads sensor -> 200 OK
        mockMvc.perform(get("/api/v1/sensors/" + sensorId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sensorId.toString()));

        // Operator lists field sensors -> 200 OK
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/sensors")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Operator attempts to register a sensor -> 403 Forbidden
        CreateSensorRequest newSensorReq = new CreateSensorRequest(
                fieldId,
                "Unauthorized Sensor",
                "SN-OP-FORBIDDEN-" + UUID.randomUUID().toString().substring(0, 6),
                SensorType.TEMPERATURE,
                "SIMULATED",
                null
        );
        mockMvc.perform(post("/api/v1/sensors")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSensorReq)))
                .andExpect(status().isForbidden());

        // Operator attempts to deactivate farmer's sensor -> 403 Forbidden
        mockMvc.perform(patch("/api/v1/sensors/" + sensorId + "/deactivate")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin has global authorization: can register, update, and manage sensors (201 / 200)")
    void admin_fullAccess_allowed() throws Exception {
        String farmerToken = registerFarmer("Otabek", "Yoqubov");
        String adminToken = createAdminToken();

        UUID fieldId = createField(farmerToken, "Otabek Bogʻi");
        String deviceId = "SN-ADM-" + UUID.randomUUID().toString().substring(0, 8);

        // Admin creates sensor on farmer's field -> 201 Created
        UUID sensorId = createSensor(adminToken, fieldId, deviceId, SensorType.TEMPERATURE);

        // Admin updates sensor -> 200 OK
        UpdateSensorRequest updateReq = new UpdateSensorRequest("Admin Calibrated Sensor", "Calibrated");
        mockMvc.perform(patch("/api/v1/sensors/" + sensorId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Admin Calibrated Sensor"));
    }

    @Test
    @DisplayName("Telemetry observation recording, pagination, time range filtering, and simulation endpoint")
    void telemetry_readingsAndSimulation_success() throws Exception {
        String farmerToken = registerFarmer("Jasur", "Nazarov");
        UUID fieldId = createField(farmerToken, "Jasur Ekinzori");
        String deviceId = "SN-TEL-" + UUID.randomUUID().toString().substring(0, 8);
        UUID sensorId = createSensor(farmerToken, fieldId, deviceId, SensorType.SOIL_MOISTURE);

        // 1. Record manual reading
        RecordSensorReadingRequest readingReq = new RecordSensorReadingRequest(
                new BigDecimal("58.20"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "INTEGRATION_TEST",
                Instant.now()
        );

        mockMvc.perform(post("/api/v1/sensors/" + sensorId + "/readings")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readingReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sensorId").value(sensorId.toString()))
                .andExpect(jsonPath("$.data.value").value(58.2000))
                .andExpect(jsonPath("$.data.unitSymbol").value("%"));

        // 2. Trigger on-demand simulation
        mockMvc.perform(post("/api/v1/sensors/" + sensorId + "/simulate")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sensorId").value(sensorId.toString()))
                .andExpect(jsonPath("$.data.source").value("SIMULATED_SENSOR_PROVIDER"));

        // 3. Get latest reading
        mockMvc.perform(get("/api/v1/sensors/" + sensorId + "/latest-reading")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sensorId").value(sensorId.toString()));

        // 4. Query readings history with pagination
        mockMvc.perform(get("/api/v1/sensors/" + sensorId + "/readings?page=0&size=10")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        // 5. Query readings with invalid date range (from > to) -> 422
        Instant now = Instant.now();
        Instant past = now.minus(2, ChronoUnit.HOURS);
        mockMvc.perform(get("/api/v1/sensors/" + sensorId + "/readings?from=" + now + "&to=" + past)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isUnprocessableEntity());

        // 6. Query field telemetry summary
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/readings/latest-summary")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.readings").isArray());
    }
}
