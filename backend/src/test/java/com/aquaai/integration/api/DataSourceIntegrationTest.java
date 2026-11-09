package com.aquaai.integration.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.integration.api.dto.CreateDataSourceRequest;
import com.aquaai.integration.api.dto.IngestTelemetryBatchRequest;
import com.aquaai.integration.api.dto.RecordManualMeasurementRequest;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import com.aquaai.sensor.domain.model.SensorType;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataSourceIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 9100000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private com.aquaai.integration.domain.repository.DataSourceRepository dataSourceRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private record RegisteredFarmer(String token, UUID userId) {}

    private RegisteredFarmer registerFarmer() throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "int.farmer." + id + "." + UUID.randomUUID().toString().substring(0, 5) + "@yangiariq.uz";

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Integration")
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
        return new RegisteredFarmer(auth.accessToken(), auth.user().id());
    }

    private String createOperatorToken() {
        long id = USER_COUNTER.incrementAndGet();
        User op = userRepository.save(User.builder()
                .firstName("Integration")
                .lastName("Operator")
                .fullName("Integration District Operator")
                .phoneNumber("+99893" + String.format("%07d", id))
                .email("int.operator." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForOperator123456789")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(op.getId(), op.getEmail(), op.getRole());
    }

    private Field createField(UUID ownerId, String name) {
        return fieldRepository.save(Field.create(
                ownerId,
                name,
                "Test parcel for integration",
                new BigDecimal("6.5"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                new FieldLocation(new BigDecimal("41.3350"), new BigDecimal("60.6010")),
                null
        ));
    }

    @Test
    @DisplayName("1. Authenticated users can list seeded data sources")
    void shouldListDataSources() throws Exception {
        RegisteredFarmer farmer = registerFarmer();

        mockMvc.perform(get("/api/v1/data-sources")
                        .header("Authorization", "Bearer " + farmer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(4)));
    }

    @Test
    @DisplayName("2. Role isolation: Operator can register data source, Farmer is forbidden (403)")
    void shouldEnforceRoleIsolationOnDataSourceRegistration() throws Exception {
        RegisteredFarmer farmer = registerFarmer();
        String operatorToken = createOperatorToken();

        String code = "GATEWAY_INT_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        CreateDataSourceRequest req = new CreateDataSourceRequest(
                code,
                "District IoT Gateway",
                DataSourceType.SENSOR_DEVICE,
                "HttpGatewayAdapter",
                "Edge gateway description",
                "https://gateway.yangiariq.uz/telemetry",
                "CONFIG:token",
                DataSourceStatus.ACTIVE
        );

        // Farmer attempts creation -> 403 Forbidden
        mockMvc.perform(post("/api/v1/data-sources")
                        .header("Authorization", "Bearer " + farmer.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        // Operator attempts creation -> 201 Created
        mockMvc.perform(post("/api/v1/data-sources")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sourceCode").value(code));
    }

    @Test
    @DisplayName("3. Operator can ingest telemetry batch for parcel")
    void shouldIngestTelemetryBatch() throws Exception {
        RegisteredFarmer farmer = registerFarmer();
        Field field = createField(farmer.userId(), "Integration Field Telemetry");
        String operatorToken = createOperatorToken();

        // Ensure gateway source is ACTIVE
        dataSourceRepository.findBySourceCode("HTTP_SENSOR_GATEWAY").ifPresent(ds -> {
            ds.activate();
            dataSourceRepository.save(ds);
        });

        var item = new IngestTelemetryBatchRequest.ItemRequest(
                "BATCH-MSG-" + UUID.randomUUID().toString().substring(0, 8),
                field.getId(),
                null,
                null,
                SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(52.5),
                "%",
                Instant.now(),
                "OBSERVED"
        );

        var batchReq = new IngestTelemetryBatchRequest("HTTP_SENSOR_GATEWAY", List.of(item));

        mockMvc.perform(post("/api/v1/data-ingestion/telemetry")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.validCount").value(1))
                .andExpect(jsonPath("$.data.rejectedCount").value(0));
    }

    @Test
    @DisplayName("4. Anti-IDOR: Farmer can record manual measurement for their own field, but NOT another farmer's field")
    void shouldEnforceAntiIdorOnManualMeasurement() throws Exception {
        RegisteredFarmer farmerA = registerFarmer();
        RegisteredFarmer farmerB = registerFarmer();

        Field fieldA = createField(farmerA.userId(), "Farmer A Field");

        RecordManualMeasurementRequest req = new RecordManualMeasurementRequest(
                fieldA.getId(),
                null,
                SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(44.0),
                "%",
                Instant.now(),
                false,
                "Farmer self-check"
        );

        // Farmer B attempts to submit for Field A -> 404/403 (ResourceNotFoundException due to anti-IDOR masking)
        mockMvc.perform(post("/api/v1/data-ingestion/manual")
                        .header("Authorization", "Bearer " + farmerB.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        // Farmer A submits for Field A -> 201 Created
        mockMvc.perform(post("/api/v1/data-ingestion/manual")
                        .header("Authorization", "Bearer " + farmerA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.validCount").value(1));
    }

    @Test
    @DisplayName("5. Quality summary endpoint aggregates ingestion health")
    void shouldGetQualitySummary() throws Exception {
        RegisteredFarmer farmer = registerFarmer();

        mockMvc.perform(get("/api/v1/data-quality/summary")
                        .header("Authorization", "Bearer " + farmer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallQualityScore").isNumber());
    }

    @Test
    @DisplayName("6. Operator can upload CSV telemetry file with dryRun preview")
    void shouldUploadCsvWithDryRun() throws Exception {
        RegisteredFarmer farmer = registerFarmer();
        Field field = createField(farmer.userId(), "CSV Target Parcel");
        String operatorToken = createOperatorToken();

        String csv = "field_id,type,value,unit\n" + field.getId() + ",SOIL_MOISTURE,50.0,%\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csv.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/data-imports/csv")
                        .file(file)
                        .param("dryRun", "true")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRows").value(1))
                .andExpect(jsonPath("$.data.successfulRows").value(1));
    }
}
