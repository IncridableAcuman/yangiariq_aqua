package com.aquaai.water.api;

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
import com.aquaai.water.api.dto.CreateIrrigationPlanRequest;
import com.aquaai.water.api.dto.CreateWaterAllocationRequest;
import com.aquaai.water.api.dto.CreateWaterSourceRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WaterConsumptionIdorTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(7000);

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
        String email = "farmer." + id + "@yangiariq.uz";

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

    private String createPrivilegedUser(String prefix, UserRole role) {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99891" + String.format("%07d", id);
        String email = prefix + "." + id + "@yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName(prefix)
                .lastName("RoleUser")
                .fullName(prefix + " RoleUser")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("$2a$10$e84WJb3hZekM7bL4.oO62O8VqC5yT.yvMvh2Z/iK2mZ2c8rO2H2.m")
                .role(role)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private UUID createTestField(String token, String fieldName) throws Exception {
        CreateFieldRequest fieldRequest = new CreateFieldRequest(
                fieldName,
                "IDOR test plot",
                new BigDecimal("10.0000"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.CANAL,
                new BigDecimal("41.450000"),
                new BigDecimal("60.590000"),
                FieldBoundary.fromExteriorRing(List.of(
                        CoordinatePoint.of(60.5900, 41.4500),
                        CoordinatePoint.of(60.5950, 41.4500),
                        CoordinatePoint.of(60.5950, 41.4550),
                        CoordinatePoint.of(60.5900, 41.4550)
                ))
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fieldRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return UUID.fromString(data.get("id").asText());
    }

    private UUID createWaterSource(String token, String name, WaterSourceType type) throws Exception {
        CreateWaterSourceRequest request = new CreateWaterSourceRequest(name, type, "Regional source");
        MvcResult result = mockMvc.perform(post("/api/v1/water-sources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return UUID.fromString(data.get("id").asText());
    }

    // =========================================================================
    // 1. Unauthenticated Protection (401 Unauthorized)
    // =========================================================================
    @Test
    @DisplayName("1. Unauthenticated requests to consumption endpoints return 401 Unauthorized")
    void testUnauthenticatedEndpoints() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/water/fields/" + randomId + "/summary"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/water/allocations/" + randomId + "/summary"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/water-allocations/" + randomId + "/consumption"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/water/plans/" + randomId + "/metrics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/irrigation-plans/" + randomId + "/metrics"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 2. IDOR Prevention & Anti-Enumeration (404 Not Found for Foreign Farmer)
    // =========================================================================
    @Test
    @DisplayName("2. IDOR Protection: Farmer B cannot access Field A summary, Allocation A summary, or Plan A metrics (404 Anti-Enumeration)")
    void testFarmerIdorProtection() throws Exception {
        String farmerAToken = registerFarmer("Rustam", "FarmerA");
        String farmerBToken = registerFarmer("Shavkat", "FarmerB");
        String operatorToken = createPrivilegedUser("opIdorWater", UserRole.OPERATOR);

        UUID fieldAId = createTestField(farmerAToken, "Private Parcel A");
        UUID sourceId = createWaterSource(operatorToken, "IDOR Canal " + System.currentTimeMillis(), WaterSourceType.CANAL);

        // Allocation A
        CreateWaterAllocationRequest allocReq = new CreateWaterAllocationRequest(
                fieldAId, sourceId, new BigDecimal("1000.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Quota A"
        );
        MvcResult allocRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocationAId = UUID.fromString(objectMapper.readTree(allocRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Plan A
        CreateIrrigationPlanRequest planReq = new CreateIrrigationPlanRequest(
                fieldAId, allocationAId, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("400.0000"), IrrigationMethod.DRIP, "Plan A"
        );
        MvcResult planRes = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID planAId = UUID.fromString(objectMapper.readTree(planRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // 2.1 Farmer B attempts GET /api/v1/water/fields/{fieldAId}/summary -> 404 Not Found
        mockMvc.perform(get("/api/v1/water/fields/" + fieldAId + "/summary")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 2.2 Farmer B attempts GET /api/v1/water/allocations/{allocationAId}/summary -> 404 Not Found
        mockMvc.perform(get("/api/v1/water/allocations/" + allocationAId + "/summary")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 2.3 Farmer B attempts GET /api/v1/water-allocations/{allocationAId}/consumption -> 404 Not Found
        mockMvc.perform(get("/api/v1/water-allocations/" + allocationAId + "/consumption")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 2.4 Farmer B attempts GET /api/v1/water/plans/{planAId}/metrics -> 404 Not Found
        mockMvc.perform(get("/api/v1/water/plans/" + planAId + "/metrics")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 2.5 Farmer B attempts GET /api/v1/irrigation-plans/{planAId}/metrics -> 404 Not Found
        mockMvc.perform(get("/api/v1/irrigation-plans/" + planAId + "/metrics")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 2.6 Farmer A accesses own resources -> 200 OK
        mockMvc.perform(get("/api/v1/water/fields/" + fieldAId + "/summary")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldAId.toString()));

        mockMvc.perform(get("/api/v1/water/allocations/" + allocationAId + "/summary")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allocationId").value(allocationAId.toString()));

        mockMvc.perform(get("/api/v1/water/plans/" + planAId + "/metrics")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(planAId.toString()));
    }

    // =========================================================================
    // 3. Privileged Access: Operator and Admin Visibility
    // =========================================================================
    @Test
    @DisplayName("3. Privileged Access: Operator and Admin can view consumption summaries across farmers")
    void testOperatorAndAdminVisibility() throws Exception {
        String farmerToken = registerFarmer("Botir", "PrivTest");
        String operatorToken = createPrivilegedUser("opPrivWater", UserRole.OPERATOR);
        String adminToken = createPrivilegedUser("adminPrivWater", UserRole.ADMIN);

        UUID fieldId = createTestField(farmerToken, "District Parcel Privileged");
        UUID sourceId = createWaterSource(operatorToken, "Priv River " + System.currentTimeMillis(), WaterSourceType.RIVER);

        CreateWaterAllocationRequest allocReq = new CreateWaterAllocationRequest(
                fieldId, sourceId, new BigDecimal("800.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "District quota"
        );
        MvcResult allocRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocId = UUID.fromString(objectMapper.readTree(allocRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Operator accesses Field Summary -> 200 OK
        mockMvc.perform(get("/api/v1/water/fields/" + fieldId + "/summary")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));

        // Operator accesses Allocation Summary -> 200 OK
        mockMvc.perform(get("/api/v1/water/allocations/" + allocId + "/summary")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allocationId").value(allocId.toString()));

        // Admin accesses Field Summary -> 200 OK
        mockMvc.perform(get("/api/v1/water/fields/" + fieldId + "/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));
    }

    // =========================================================================
    // 4. Invalid Date Range Protection (422 Unprocessable Entity)
    // =========================================================================
    @Test
    @DisplayName("4. Validation: 'from' timestamp after 'to' timestamp is rejected with 422 Unprocessable Entity")
    void testInvalidDateRange() throws Exception {
        String farmerToken = registerFarmer("Alisher", "DateRangeTest");
        UUID fieldId = createTestField(farmerToken, "Date Range Parcel");

        Instant t0 = Instant.now();
        Instant tBefore = t0.minus(5, ChronoUnit.DAYS);

        // from = t0, to = tBefore (from is AFTER to)
        mockMvc.perform(get("/api/v1/water/fields/" + fieldId + "/summary")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("from", t0.toString())
                        .param("to", tBefore.toString()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_DATE_RANGE"));
    }
}
