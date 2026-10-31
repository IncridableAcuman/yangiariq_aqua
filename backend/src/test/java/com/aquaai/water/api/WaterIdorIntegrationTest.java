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
import com.aquaai.water.api.dto.CancelOperationRequest;
import com.aquaai.water.api.dto.CompleteIrrigationEventRequest;
import com.aquaai.water.api.dto.CreateIrrigationEventRequest;
import com.aquaai.water.api.dto.CreateIrrigationPlanRequest;
import com.aquaai.water.api.dto.CreateWaterAllocationRequest;
import com.aquaai.water.api.dto.CreateWaterSourceRequest;
import com.aquaai.water.api.dto.RecordCompletedIrrigationEventRequest;
import com.aquaai.water.api.dto.UpdateIrrigationPlanRequest;
import com.aquaai.water.api.dto.UpdateWaterAllocationRequest;
import com.aquaai.water.api.dto.UpdateWaterSourceRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WaterIdorIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(5000);

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
                "IDOR isolation field",
                new BigDecimal("8.0000"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.CANAL,
                new BigDecimal("41.451000"),
                new BigDecimal("60.591000"),
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
    // 1. Unauthenticated Security Boundary (401 Unauthorized)
    // =========================================================================
    @Test
    @DisplayName("1. Unauthenticated requests to all water domain endpoints return 401 Unauthorized")
    void testUnauthenticatedEndpointsReturn401() throws Exception {
        UUID randomId = UUID.randomUUID();

        // Water Sources
        mockMvc.perform(post("/api/v1/water-sources").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/water-sources/" + randomId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/water-sources"))
                .andExpect(status().isUnauthorized());

        // Water Allocations
        mockMvc.perform(post("/api/v1/water-allocations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/water-allocations/" + randomId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/water-allocations"))
                .andExpect(status().isUnauthorized());

        // Irrigation Plans
        mockMvc.perform(post("/api/v1/irrigation-plans").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/irrigation-plans/" + randomId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/irrigation-plans"))
                .andExpect(status().isUnauthorized());

        // Irrigation Events
        mockMvc.perform(post("/api/v1/irrigation-events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/irrigation-events/completed").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/irrigation-events/" + randomId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/irrigation-events"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 2. WaterSource Role Authorization (403 Forbidden for Farmers)
    // =========================================================================
    @Test
    @DisplayName("2. Role Security: Farmers cannot create, update, activate, or deactivate water sources")
    void testFarmerRoleWaterSourceRestrictions() throws Exception {
        String farmerToken = registerFarmer("Rustam", "Farmer");
        String operatorToken = createPrivilegedUser("opWaterSourceRole", UserRole.OPERATOR);

        UUID sourceId = createWaterSource(operatorToken, "Infrastructure Canal " + System.currentTimeMillis(), WaterSourceType.CANAL);

        // 2.1 Farmer cannot create water source
        CreateWaterSourceRequest createReq = new CreateWaterSourceRequest("Unauthorized Canal", WaterSourceType.CANAL, "Test");
        mockMvc.perform(post("/api/v1/water-sources")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // 2.2 Farmer cannot update water source
        UpdateWaterSourceRequest updateReq = new UpdateWaterSourceRequest("Hacked Canal", WaterSourceType.RESERVOIR, "Hacked");
        mockMvc.perform(patch("/api/v1/water-sources/" + sourceId)
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // 2.3 Farmer cannot activate water source
        mockMvc.perform(patch("/api/v1/water-sources/" + sourceId + "/activate")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // 2.4 Farmer cannot deactivate water source
        mockMvc.perform(patch("/api/v1/water-sources/" + sourceId + "/deactivate")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    // =========================================================================
    // 3. WaterAllocation IDOR & Anti-Enumeration (404 Not Found)
    // =========================================================================
    @Test
    @DisplayName("3. WaterAllocation IDOR: Farmer B cannot create, read, update, close, or cancel Farmer A's allocation")
    void testWaterAllocationIdorProtection() throws Exception {
        String farmerAToken = registerFarmer("Aziz", "FarmerA");
        String farmerBToken = registerFarmer("Bekzod", "FarmerB");
        String operatorToken = createPrivilegedUser("opAllocIdor", UserRole.OPERATOR);

        UUID fieldAId = createTestField(farmerAToken, "Farmer A Private Parcel");
        UUID sourceId = createWaterSource(operatorToken, "IDOR Canal " + System.currentTimeMillis(), WaterSourceType.CANAL);

        // 3.1 Farmer B tries to allocate water to Farmer A's field -> 404 (Anti-enumeration)
        CreateWaterAllocationRequest idorCreateReq = new CreateWaterAllocationRequest(
                fieldAId, sourceId, new BigDecimal("1000.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Malicious allocation"
        );
        mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idorCreateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // Farmer A creates valid allocation
        CreateWaterAllocationRequest validReq = new CreateWaterAllocationRequest(
                fieldAId, sourceId, new BigDecimal("1500.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Legit allocation"
        );
        MvcResult createRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocationAId = UUID.fromString(objectMapper.readTree(createRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // 3.2 Farmer B attempts GET /water-allocations/{allocationAId} -> 404
        mockMvc.perform(get("/api/v1/water-allocations/" + allocationAId)
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 3.3 Farmer B attempts PATCH /water-allocations/{allocationAId} -> 404
        UpdateWaterAllocationRequest updateReq = new UpdateWaterAllocationRequest(
                new BigDecimal("9999.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Tampered volume"
        );
        mockMvc.perform(patch("/api/v1/water-allocations/" + allocationAId)
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 3.4 Farmer B attempts PATCH /water-allocations/{allocationAId}/close -> 404
        mockMvc.perform(patch("/api/v1/water-allocations/" + allocationAId + "/close")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 3.5 Farmer B attempts PATCH /water-allocations/{allocationAId}/cancel -> 404
        mockMvc.perform(patch("/api/v1/water-allocations/" + allocationAId + "/cancel")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    // =========================================================================
    // 4. IrrigationPlan IDOR & Anti-Enumeration (404 Not Found)
    // =========================================================================
    @Test
    @DisplayName("4. IrrigationPlan IDOR: Farmer B cannot create, read, update, start, complete, or cancel Farmer A's plan")
    void testIrrigationPlanIdorProtection() throws Exception {
        String farmerAToken = registerFarmer("Dilshod", "FarmerA");
        String farmerBToken = registerFarmer("Elbek", "FarmerB");
        String operatorToken = createPrivilegedUser("opPlanIdor", UserRole.OPERATOR);

        UUID fieldAId = createTestField(farmerAToken, "Farmer A Plan Parcel");
        UUID sourceId = createWaterSource(operatorToken, "Plan IDOR Canal " + System.currentTimeMillis(), WaterSourceType.CANAL);

        // Farmer A creates allocation
        CreateWaterAllocationRequest allocReq = new CreateWaterAllocationRequest(
                fieldAId, sourceId, new BigDecimal("1000.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Allocation A"
        );
        MvcResult allocRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocationAId = UUID.fromString(objectMapper.readTree(allocRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // 4.1 Farmer B attempts to schedule plan on Field A -> 404
        CreateIrrigationPlanRequest idorPlanReq = new CreateIrrigationPlanRequest(
                fieldAId, allocationAId, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("300.0000"), IrrigationMethod.FURROW, "Malicious plan"
        );
        mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idorPlanReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // Farmer A schedules valid plan
        MvcResult planRes = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idorPlanReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID planAId = UUID.fromString(objectMapper.readTree(planRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // 4.2 Farmer B attempts GET /irrigation-plans/{planAId} -> 404
        mockMvc.perform(get("/api/v1/irrigation-plans/" + planAId)
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 4.3 Farmer B attempts PATCH /irrigation-plans/{planAId} -> 404
        UpdateIrrigationPlanRequest updatePlanReq = new UpdateIrrigationPlanRequest(
                allocationAId, Instant.now().plus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("999.0000"), IrrigationMethod.DRIP, "Tampered plan"
        );
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planAId)
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePlanReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 4.4 Farmer B attempts PATCH /irrigation-plans/{planAId}/start -> 404
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planAId + "/start")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 4.5 Farmer B attempts PATCH /irrigation-plans/{planAId}/complete -> 404
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planAId + "/complete")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 4.6 Farmer B attempts PATCH /irrigation-plans/{planAId}/cancel -> 404
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planAId + "/cancel")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    // =========================================================================
    // 5. IrrigationEvent IDOR & Anti-Enumeration (404 Not Found)
    // =========================================================================
    @Test
    @DisplayName("5. IrrigationEvent IDOR: Farmer B cannot start, record, view, complete, or cancel Farmer A's event")
    void testIrrigationEventIdorProtection() throws Exception {
        String farmerAToken = registerFarmer("Gulom", "FarmerA");
        String farmerBToken = registerFarmer("Hamid", "FarmerB");

        UUID fieldAId = createTestField(farmerAToken, "Farmer A Event Parcel");

        // 5.1 Farmer B attempts to start event on Field A -> 404
        CreateIrrigationEventRequest idorStartReq = new CreateIrrigationEventRequest(fieldAId, null, Instant.now(), "Malicious start");
        mockMvc.perform(post("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idorStartReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 5.2 Farmer B attempts to record completed event on Field A -> 404
        RecordCompletedIrrigationEventRequest idorCompletedReq = new RecordCompletedIrrigationEventRequest(
                fieldAId, null, Instant.now().minus(2, ChronoUnit.HOURS), Instant.now(), new BigDecimal("200.0000"), "Malicious record"
        );
        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idorCompletedReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // Farmer A starts valid event
        MvcResult eventRes = mockMvc.perform(post("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idorStartReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID eventAId = UUID.fromString(objectMapper.readTree(eventRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // 5.3 Farmer B attempts GET /irrigation-events/{eventAId} -> 404
        mockMvc.perform(get("/api/v1/irrigation-events/" + eventAId)
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 5.4 Farmer B attempts PATCH /irrigation-events/{eventAId}/complete -> 404
        CompleteIrrigationEventRequest completeReq = new CompleteIrrigationEventRequest(
                Instant.now(), new BigDecimal("250.0000"), "Tampered finish"
        );
        mockMvc.perform(patch("/api/v1/irrigation-events/" + eventAId + "/complete")
                        .header("Authorization", "Bearer " + farmerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 5.5 Farmer B attempts PATCH /irrigation-events/{eventAId}/cancel -> 404
        mockMvc.perform(patch("/api/v1/irrigation-events/" + eventAId + "/cancel")
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    // =========================================================================
    // 6. Cross-Aggregate Integrity & Invariant Tampering (422 Unprocessable Entity)
    // =========================================================================
    @Test
    @DisplayName("6. Cross-Aggregate Invariants: Reject cross-field allocation/plan tampering and inactive water sources")
    void testCrossAggregateTamperingRestrictions() throws Exception {
        String farmerToken = registerFarmer("Ilhom", "FarmerX");
        String operatorToken = createPrivilegedUser("opTampering", UserRole.OPERATOR);

        UUID field1Id = createTestField(farmerToken, "Field Alpha");
        UUID field2Id = createTestField(farmerToken, "Field Beta");

        UUID activeSourceId = createWaterSource(operatorToken, "Active Canal " + System.currentTimeMillis(), WaterSourceType.CANAL);
        UUID inactiveSourceId = createWaterSource(operatorToken, "Inactive Canal " + System.currentTimeMillis(), WaterSourceType.CANAL);

        // Deactivate inactiveSourceId
        mockMvc.perform(patch("/api/v1/water-sources/" + inactiveSourceId + "/deactivate")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        // 6.1 Creating allocation from INACTIVE water source fails with 422
        CreateWaterAllocationRequest inactiveAllocReq = new CreateWaterAllocationRequest(
                field1Id, inactiveSourceId, new BigDecimal("1000.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Quota on dead source"
        );
        mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inactiveAllocReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"));

        // Create valid allocation for field2
        CreateWaterAllocationRequest allocField2Req = new CreateWaterAllocationRequest(
                field2Id, activeSourceId, new BigDecimal("800.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Field2 allocation"
        );
        MvcResult alloc2Res = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocField2Req)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID alloc2Id = UUID.fromString(objectMapper.readTree(alloc2Res.getResponse().getContentAsString()).get("data").get("id").asText());

        // 6.2 Farmer creates plan on Field 1, but attempts to link Allocation from Field 2 -> 422
        CreateIrrigationPlanRequest tamperedPlanReq = new CreateIrrigationPlanRequest(
                field1Id, alloc2Id, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("400.0000"), IrrigationMethod.DRIP, "Cross-field allocation plan"
        );
        mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tamperedPlanReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"));

        // Create valid plan for Field 2
        CreateIrrigationPlanRequest validPlanReq = new CreateIrrigationPlanRequest(
                field2Id, alloc2Id, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("400.0000"), IrrigationMethod.DRIP, "Field 2 Plan"
        );
        MvcResult plan2Res = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlanReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID plan2Id = UUID.fromString(objectMapper.readTree(plan2Res.getResponse().getContentAsString()).get("data").get("id").asText());

        // 6.3 Farmer starts event on Field 1, but links Plan from Field 2 -> 422
        CreateIrrigationEventRequest tamperedEventReq = new CreateIrrigationEventRequest(
                field1Id, plan2Id, Instant.now(), "Cross-field plan event"
        );
        mockMvc.perform(post("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tamperedEventReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"));
    }

    // =========================================================================
    // 7. Privileged Operator and Admin District-Wide Visibility
    // =========================================================================
    @Test
    @DisplayName("7. Privileged Visibility: Operator and Admin can inspect allocations, plans, and events across farmers")
    void testOperatorAndAdminDistrictWideAccess() throws Exception {
        String farmerToken = registerFarmer("Kamron", "FarmerDistrict");
        String operatorToken = createPrivilegedUser("opDistrictMonitor", UserRole.OPERATOR);
        String adminToken = createPrivilegedUser("adminDistrictMonitor", UserRole.ADMIN);

        UUID fieldId = createTestField(farmerToken, "District Parcel");
        UUID sourceId = createWaterSource(operatorToken, "District River " + System.currentTimeMillis(), WaterSourceType.RIVER);

        // Farmer creates allocation
        CreateWaterAllocationRequest allocReq = new CreateWaterAllocationRequest(
                fieldId, sourceId, new BigDecimal("2000.0000"), Instant.now(), Instant.now().plus(45, ChronoUnit.DAYS), "District quota"
        );
        MvcResult allocRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocId = UUID.fromString(objectMapper.readTree(allocRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Operator can get allocation
        mockMvc.perform(get("/api/v1/water-allocations/" + allocId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(allocId.toString()));

        // Admin can get allocation
        mockMvc.perform(get("/api/v1/water-allocations/" + allocId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(allocId.toString()));

        // Operator lists allocations district-wide with field filter
        mockMvc.perform(get("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + operatorToken)
                        .param("fieldId", fieldId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(allocId.toString()));
    }
}
