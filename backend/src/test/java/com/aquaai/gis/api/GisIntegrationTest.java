package com.aquaai.gis.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.aquaai.water.api.dto.CreateWaterAllocationRequest;
import com.aquaai.water.api.dto.CreateWaterSourceRequest;
import com.aquaai.water.api.dto.RecordCompletedIrrigationEventRequest;
import com.aquaai.water.domain.model.IrrigationMethod;
import com.aquaai.water.domain.model.WaterSourceType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GisIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(8000);

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
        String email = "farmer." + id + "@gis.yangiariq.uz";

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
        String email = "operator." + id + "@gis.yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("District")
                .fullName("Operator District")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("$2a$10$e84WJb3hZekM7bL4.oO62O8VqC5yT.yvMvh2Z/iK2mZ2c8rO2H2.m")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private UUID createField(String token, String name, double lat, double lon) throws Exception {
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(lon, lat),
                CoordinatePoint.of(lon + 0.01, lat),
                CoordinatePoint.of(lon + 0.01, lat + 0.01),
                CoordinatePoint.of(lon, lat + 0.01),
                CoordinatePoint.of(lon, lat)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                name,
                "GIS parcel",
                new BigDecimal("5.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.DRIP,
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

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return UUID.fromString(data.get("id").asText());
    }

    private UUID createWaterSource(String operatorToken, String name, double lat, double lon) throws Exception {
        CreateWaterSourceRequest request = new CreateWaterSourceRequest(
                name,
                WaterSourceType.CANAL,
                "GIS feeder canal",
                new BigDecimal(String.valueOf(lat)),
                new BigDecimal(String.valueOf(lon))
        );

        MvcResult result = mockMvc.perform(post("/api/v1/water-sources")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return UUID.fromString(data.get("id").asText());
    }

    private UUID createWaterAllocation(String token, UUID fieldId, UUID waterSourceId, BigDecimal volume) throws Exception {
        Instant now = Instant.now();
        CreateWaterAllocationRequest request = new CreateWaterAllocationRequest(
                fieldId,
                waterSourceId,
                volume,
                now.minus(10, ChronoUnit.DAYS),
                now.plus(60, ChronoUnit.DAYS),
                "GIS Seasonal Allocation"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return UUID.fromString(data.get("id").asText());
    }

    @Test
    @DisplayName("Unauthenticated request to GIS overview returns 401 Unauthorized")
    void unauthenticatedRequestShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/gis/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer sees strictly own fields and allocations with anti-IDOR isolation")
    void farmerShouldSeeOnlyOwnFieldsAndAllocations() throws Exception {
        String farmerAToken = registerFarmer("Anvar", "Karimov");
        String farmerBToken = registerFarmer("Bekzod", "Sultonov");
        String operatorToken = createOperatorToken();

        UUID fieldA1 = createField(farmerAToken, "Anvar Field 1", 41.3414, 60.6094);
        UUID fieldA2 = createField(farmerAToken, "Anvar Field 2", 41.3500, 60.6200);
        UUID fieldB1 = createField(farmerBToken, "Bekzod Field 1", 41.3600, 60.6300);

        UUID canal1 = createWaterSource(operatorToken, "Palvon Canal GIS " + UUID.randomUUID(), 41.3450, 60.6150);
        UUID canal2 = createWaterSource(operatorToken, "Qiyot Canal GIS " + UUID.randomUUID(), 41.3550, 60.6250);

        createWaterAllocation(farmerAToken, fieldA1, canal1, new BigDecimal("1000.0000"));
        createWaterAllocation(farmerBToken, fieldB1, canal2, new BigDecimal("2000.0000"));

        // Farmer A queries overview
        MvcResult resultA = mockMvc.perform(get("/api/v1/gis/overview")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode dataA = objectMapper.readTree(resultA.getResponse().getContentAsString()).get("data");
        JsonNode fieldsA = dataA.get("fields");
        JsonNode allocationsA = dataA.get("allocations");
        JsonNode waterSourcesA = dataA.get("waterSources");
        JsonNode statusesA = dataA.get("waterStatuses");

        // Farmer A must see Field A1 and A2, and MUST NOT see Field B1
        assertThat(fieldsA).isNotEmpty();
        List<String> fieldIdsA = new java.util.ArrayList<>();
        for (JsonNode f : fieldsA) {
            fieldIdsA.add(f.get("id").asText());
        }
        assertThat(fieldIdsA).contains(fieldA1.toString(), fieldA2.toString());
        assertThat(fieldIdsA).doesNotContain(fieldB1.toString());

        // Farmer A must see only their own allocation
        List<String> allocFieldIds = new java.util.ArrayList<>();
        for (JsonNode a : allocationsA) {
            allocFieldIds.add(a.get("fieldId").asText());
        }
        assertThat(allocFieldIds).contains(fieldA1.toString());
        assertThat(allocFieldIds).doesNotContain(fieldB1.toString());

        // Water sources are district shared infrastructure
        assertThat(waterSourcesA.size()).isGreaterThanOrEqualTo(2);

        // Water statuses only calculated for Farmer A's fields
        List<String> statusFieldIds = new java.util.ArrayList<>();
        for (JsonNode s : statusesA) {
            statusFieldIds.add(s.get("fieldId").asText());
        }
        assertThat(statusFieldIds).contains(fieldA1.toString(), fieldA2.toString());
        assertThat(statusFieldIds).doesNotContain(fieldB1.toString());
    }

    @Test
    @DisplayName("Operator has district-wide visibility over all fields and allocations")
    void operatorShouldSeeAllDistrictFields() throws Exception {
        String farmerToken = registerFarmer("Jasur", "Rahimov");
        String operatorToken = createOperatorToken();

        UUID fieldId = createField(farmerToken, "Jasur Parcel", 41.3414, 60.6094);
        UUID canalId = createWaterSource(operatorToken, "North Canal " + UUID.randomUUID(), 41.3480, 60.6180);
        createWaterAllocation(farmerToken, fieldId, canalId, new BigDecimal("1500.0000"));

        MvcResult result = mockMvc.perform(get("/api/v1/gis/overview")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        JsonNode fields = data.get("fields");

        List<String> fieldIds = new java.util.ArrayList<>();
        for (JsonNode f : fields) {
            fieldIds.add(f.get("id").asText());
        }
        assertThat(fieldIds).contains(fieldId.toString());
    }

    @Test
    @DisplayName("Over-consumption on parcel is accurately flagged with OVER_CONSUMED category")
    void overConsumptionShouldBeFlaggedAccurately() throws Exception {
        String farmerToken = registerFarmer("Otabek", "Yusupov");
        String operatorToken = createOperatorToken();

        UUID fieldId = createField(farmerToken, "Otabek Cotton", 41.3414, 60.6094);
        UUID canalId = createWaterSource(operatorToken, "Trunk Canal " + UUID.randomUUID(), 41.3480, 60.6180);
        createWaterAllocation(farmerToken, fieldId, canalId, new BigDecimal("1000.0000"));

        // Record completed irrigation of 1300 m³ (300 m³ over quota)
        Instant now = Instant.now();
        RecordCompletedIrrigationEventRequest recordReq = new RecordCompletedIrrigationEventRequest(
                fieldId,
                null,
                now.minus(4, ChronoUnit.HOURS),
                now.minus(1, ChronoUnit.HOURS),
                new BigDecimal("1300.0000"),
                "Excess delivery due to high evapotranspiration"
        );

        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordReq)))
                .andExpect(status().isCreated());

        // Check GIS overview
        MvcResult result = mockMvc.perform(get("/api/v1/gis/overview")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        JsonNode statuses = data.get("waterStatuses");

        JsonNode targetStatus = null;
        for (JsonNode s : statuses) {
            if (s.get("fieldId").asText().equals(fieldId.toString())) {
                targetStatus = s;
                break;
            }
        }

        assertThat(targetStatus).isNotNull();
        assertThat(targetStatus.get("totalAllocatedVolumeM3").asDouble()).isEqualTo(1000.0);
        assertThat(targetStatus.get("totalConsumedVolumeM3").asDouble()).isEqualTo(1300.0);
        assertThat(targetStatus.get("totalRemainingVolumeM3").asDouble()).isEqualTo(0.0);
        assertThat(targetStatus.get("totalOverConsumedVolumeM3").asDouble()).isEqualTo(300.0);
        assertThat(targetStatus.get("isOverConsumed").asBoolean()).isTrue();
        assertThat(targetStatus.get("category").asText()).isEqualTo("OVER_CONSUMED");
    }

    @Test
    @DisplayName("Invalid date range where from > to returns 422 Unprocessable Entity")
    void invalidDateRangeShouldReturn422() throws Exception {
        String farmerToken = registerFarmer("Dilshod", "Alimov");

        mockMvc.perform(get("/api/v1/gis/overview")
                        .param("from", "2026-06-01T00:00:00Z")
                        .param("to", "2026-05-01T00:00:00Z")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_DATE_RANGE"));
    }
}
