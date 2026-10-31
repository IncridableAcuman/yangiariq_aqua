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
class WaterIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(4000);

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
                .lastName("Operator")
                .fullName(prefix + " Operator")
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
                "Irrigation test plot",
                new BigDecimal("12.5000"),
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

    private UUID createWaterSource(String token, String name, WaterSourceType type, String description) throws Exception {
        CreateWaterSourceRequest request = new CreateWaterSourceRequest(name, type, description);
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
    // 1. WaterSource API: Full CRUD & Lifecycle
    // =========================================================================
    @Test
    @DisplayName("1. Operator creates, updates, deactivates, reactivates, and lists water sources")
    void testWaterSourceLifecycle() throws Exception {
        String operatorToken = createPrivilegedUser("opSource", UserRole.OPERATOR);
        String farmerToken = registerFarmer("Shavkat", "Mirziyoyev");

        // 1.1 Create Water Source
        String uniqueName = "Yangiariq Main Canal " + System.currentTimeMillis();
        UUID sourceId = createWaterSource(operatorToken, uniqueName, WaterSourceType.CANAL, "Primary district intake");

        // 1.2 Farmer can read water source
        mockMvc.perform(get("/api/v1/water-sources/" + sourceId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sourceId.toString()))
                .andExpect(jsonPath("$.data.name").value(uniqueName))
                .andExpect(jsonPath("$.data.type").value("CANAL"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // 1.3 Operator updates description and type
        UpdateWaterSourceRequest updateRequest = new UpdateWaterSourceRequest(
                uniqueName, WaterSourceType.CANAL, "Updated intake canal capacity description"
        );
        mockMvc.perform(patch("/api/v1/water-sources/" + sourceId)
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated intake canal capacity description"));

        // 1.4 Operator deactivates water source
        mockMvc.perform(patch("/api/v1/water-sources/" + sourceId + "/deactivate")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        // 1.5 Operator reactivates water source
        mockMvc.perform(patch("/api/v1/water-sources/" + sourceId + "/activate")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // 1.6 List water sources with pagination & filter
        mockMvc.perform(get("/api/v1/water-sources")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("type", "CANAL")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());

        // 1.7 Attempt to create active water source with duplicate name fails with 409 Conflict
        CreateWaterSourceRequest duplicateRequest = new CreateWaterSourceRequest(uniqueName, WaterSourceType.CANAL, "Duplicate");
        mockMvc.perform(post("/api/v1/water-sources")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    // =========================================================================
    // 2. WaterAllocation API: Full CRUD & Lifecycle
    // =========================================================================
    @Test
    @DisplayName("2. Farmer creates water allocation, updates volume, queries by field, and closes it")
    void testWaterAllocationLifecycle() throws Exception {
        String farmerToken = registerFarmer("Otabek", "Umarov");
        String operatorToken = createPrivilegedUser("opAlloc", UserRole.OPERATOR);

        UUID fieldId = createTestField(farmerToken, "Allocation Test Field");
        UUID sourceId = createWaterSource(operatorToken, "Amudaryo Intake " + System.currentTimeMillis(), WaterSourceType.RIVER, "Main river feeder");

        Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant end = start.plus(60, ChronoUnit.DAYS);

        // 2.1 Farmer creates water allocation quota
        CreateWaterAllocationRequest createRequest = new CreateWaterAllocationRequest(
                fieldId,
                sourceId,
                new BigDecimal("2500.0000"),
                start,
                end,
                "Spring 2026 cotton quota"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.waterSourceId").value(sourceId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        UUID allocationId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // 2.2 Farmer gets allocation by ID
        mockMvc.perform(get("/api/v1/water-allocations/" + allocationId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(allocationId.toString()))
                .andExpect(jsonPath("$.data.allocatedVolumeM3").value(2500.0000));

        // 2.3 Farmer updates allocation details
        UpdateWaterAllocationRequest updateRequest = new UpdateWaterAllocationRequest(
                new BigDecimal("2800.0000"),
                start,
                end.plus(15, ChronoUnit.DAYS),
                "Increased quota for second weeding"
        );

        mockMvc.perform(patch("/api/v1/water-allocations/" + allocationId)
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allocatedVolumeM3").value(2800.0000))
                .andExpect(jsonPath("$.data.notes").value("Increased quota for second weeding"));

        // 2.4 List allocations with fieldId filter
        mockMvc.perform(get("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("fieldId", fieldId.toString())
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(allocationId.toString()));

        // 2.5 Close allocation
        CancelOperationRequest closeRequest = new CancelOperationRequest("Season ended normally");
        mockMvc.perform(patch("/api/v1/water-allocations/" + allocationId + "/close")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        // 2.6 Attempt to update closed allocation fails with 422
        mockMvc.perform(patch("/api/v1/water-allocations/" + allocationId)
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("3. Farmer can cancel an active water allocation")
    void testWaterAllocationCancelLifecycle() throws Exception {
        String farmerToken = registerFarmer("Alisher", "Navoiy");
        String operatorToken = createPrivilegedUser("opAllocCancel", UserRole.OPERATOR);

        UUID fieldId = createTestField(farmerToken, "Cancel Allocation Parcel");
        UUID sourceId = createWaterSource(operatorToken, "South Well " + System.currentTimeMillis(), WaterSourceType.WELL, "Artesian well");

        CreateWaterAllocationRequest createRequest = new CreateWaterAllocationRequest(
                fieldId,
                sourceId,
                new BigDecimal("500.0000"),
                Instant.now(),
                Instant.now().plus(10, ChronoUnit.DAYS),
                "Temporary well allocation"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID allocationId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asText());

        CancelOperationRequest cancelRequest = new CancelOperationRequest("Well malfunction cut quota");
        mockMvc.perform(patch("/api/v1/water-allocations/" + allocationId + "/cancel")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    // =========================================================================
    // 3. IrrigationPlan API: Full CRUD & Execution State Machine
    // =========================================================================
    @Test
    @DisplayName("4. Farmer creates, updates, starts, and completes an irrigation plan")
    void testIrrigationPlanLifecycle() throws Exception {
        String farmerToken = registerFarmer("Bobur", "Zahiriddin");
        String operatorToken = createPrivilegedUser("opPlan", UserRole.OPERATOR);

        UUID fieldId = createTestField(farmerToken, "Corn Field 2");
        UUID sourceId = createWaterSource(operatorToken, "Ditch 4 " + System.currentTimeMillis(), WaterSourceType.DITCH, "Branch ditch");

        CreateWaterAllocationRequest allocReq = new CreateWaterAllocationRequest(
                fieldId, sourceId, new BigDecimal("1000.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Corn quota"
        );
        MvcResult allocRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocationId = UUID.fromString(objectMapper.readTree(allocRes.getResponse().getContentAsString()).get("data").get("id").asText());

        Instant planStart = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Instant planEnd = planStart.plus(8, ChronoUnit.HOURS);

        // 4.1 Create plan
        CreateIrrigationPlanRequest planReq = new CreateIrrigationPlanRequest(
                fieldId, allocationId, planStart, planEnd, new BigDecimal("450.0000"), IrrigationMethod.DRIP, "First vegetative drip cycle"
        );
        MvcResult planRes = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.status").value("PLANNED"))
                .andExpect(jsonPath("$.data.method").value("DRIP"))
                .andReturn();
        UUID planId = UUID.fromString(objectMapper.readTree(planRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // 4.2 Get plan by ID
        mockMvc.perform(get("/api/v1/irrigation-plans/" + planId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(planId.toString()))
                .andExpect(jsonPath("$.data.plannedVolumeM3").value(450.0000));

        // 4.3 Update plan details
        UpdateIrrigationPlanRequest updateReq = new UpdateIrrigationPlanRequest(
                allocationId, planStart, planEnd.plus(2, ChronoUnit.HOURS), new BigDecimal("500.0000"), IrrigationMethod.DRIP, "Extended duration"
        );
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planId)
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plannedVolumeM3").value(500.0000))
                .andExpect(jsonPath("$.data.notes").value("Extended duration"));

        // 4.4 Start plan -> IN_PROGRESS
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planId + "/start")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 4.5 Complete plan -> COMPLETED
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planId + "/complete")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // 4.6 List plans with fieldId and status filter
        mockMvc.perform(get("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("fieldId", fieldId.toString())
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(planId.toString()));
    }

    @Test
    @DisplayName("5. Farmer can cancel a planned irrigation schedule")
    void testIrrigationPlanCancelLifecycle() throws Exception {
        String farmerToken = registerFarmer("Temur", "Amir");
        UUID fieldId = createTestField(farmerToken, "Wheat Parcel");

        Instant planStart = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Instant planEnd = planStart.plus(4, ChronoUnit.HOURS);

        CreateIrrigationPlanRequest planReq = new CreateIrrigationPlanRequest(
                fieldId, null, planStart, planEnd, new BigDecimal("300.0000"), IrrigationMethod.FURROW, "Rain forecasted maybe"
        );
        MvcResult res = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID planId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("id").asText());

        CancelOperationRequest cancelReq = new CancelOperationRequest("Cancelled due to heavy rainfall");
        mockMvc.perform(patch("/api/v1/irrigation-plans/" + planId + "/cancel")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    // =========================================================================
    // 4. IrrigationEvent API: Execution, Consumption Recording & Cancellation
    // =========================================================================
    @Test
    @DisplayName("6. Farmer starts real-time event and completes it with measured volume")
    void testIrrigationEventRealtimeLifecycle() throws Exception {
        String farmerToken = registerFarmer("Jaloliddin", "Manguberdi");
        UUID fieldId = createTestField(farmerToken, "Rice Field Alpha");

        Instant startAt = Instant.now().minus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);

        // 6.1 Start event
        CreateIrrigationEventRequest startReq = new CreateIrrigationEventRequest(fieldId, null, startAt, "Pumping started");
        MvcResult startRes = mockMvc.perform(post("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.status").value("STARTED"))
                .andExpect(jsonPath("$.data.actualVolumeM3").doesNotExist())
                .andReturn();
        UUID eventId = UUID.fromString(objectMapper.readTree(startRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // 6.2 Get event by ID
        mockMvc.perform(get("/api/v1/irrigation-events/" + eventId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(eventId.toString()))
                .andExpect(jsonPath("$.data.status").value("STARTED"));

        // 6.3 Complete event with measured volume
        Instant endAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        CompleteIrrigationEventRequest completeReq = new CompleteIrrigationEventRequest(
                endAt, new BigDecimal("482.5000"), "Irrigation completed normally via mechanical water meter"
        );
        mockMvc.perform(patch("/api/v1/irrigation-events/" + eventId + "/complete")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.actualVolumeM3").value(482.5000))
                .andExpect(jsonPath("$.data.endedAt").isString());

        // 6.4 List events filtered by field
        mockMvc.perform(get("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("fieldId", fieldId.toString())
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(eventId.toString()));
    }

    @Test
    @DisplayName("7. Farmer records directly completed irrigation event")
    void testIrrigationEventDirectCompletedLifecycle() throws Exception {
        String farmerToken = registerFarmer("Farhod", "Shirin");
        UUID fieldId = createTestField(farmerToken, "Melon Plot");

        Instant startAt = Instant.now().minus(5, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        Instant endAt = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);

        RecordCompletedIrrigationEventRequest completedReq = new RecordCompletedIrrigationEventRequest(
                fieldId, null, startAt, endAt, new BigDecimal("320.0000"), "Night furrow watering"
        );

        MvcResult res = mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completedReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.actualVolumeM3").value(320.0000))
                .andReturn();

        UUID eventId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("id").asText());
        mockMvc.perform(get("/api/v1/irrigation-events/" + eventId)
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("8. Farmer cancels an in-progress irrigation event")
    void testIrrigationEventCancelLifecycle() throws Exception {
        String farmerToken = registerFarmer("Layli", "Majnun");
        UUID fieldId = createTestField(farmerToken, "Cotton Emergency Field");

        CreateIrrigationEventRequest startReq = new CreateIrrigationEventRequest(
                fieldId, null, Instant.now(), "Starting pump"
        );
        MvcResult res = mockMvc.perform(post("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("id").asText());

        CancelOperationRequest cancelReq = new CancelOperationRequest("Pump pipeline breach - stopped immediately");
        mockMvc.perform(patch("/api/v1/irrigation-events/" + eventId + "/cancel")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
