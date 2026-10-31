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
class WaterConsumptionIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(6000);

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
                "Consumption test plot",
                new BigDecimal("15.0000"),
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
    // 1. Allocation Consumption: Completed vs Started vs Cancelled Events
    // =========================================================================
    @Test
    @DisplayName("1. Allocation consumption aggregates only COMPLETED events; STARTED and CANCELLED are excluded")
    void testAllocationConsumptionEventStatusFiltering() throws Exception {
        String farmerToken = registerFarmer("Anvar", "ConsumptionTest");
        String operatorToken = createPrivilegedUser("opConsFilter", UserRole.OPERATOR);

        UUID fieldId = createTestField(farmerToken, "Status Filter Field");
        UUID sourceId = createWaterSource(operatorToken, "Filter Canal " + System.currentTimeMillis(), WaterSourceType.CANAL);

        // Create Allocation (1000 m³)
        CreateWaterAllocationRequest allocReq = new CreateWaterAllocationRequest(
                fieldId, sourceId, new BigDecimal("1000.0000"), Instant.now(), Instant.now().plus(60, ChronoUnit.DAYS), "Quota 1"
        );
        MvcResult allocRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocId = UUID.fromString(objectMapper.readTree(allocRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Create Plan 1 (linked to allocation)
        CreateIrrigationPlanRequest plan1Req = new CreateIrrigationPlanRequest(
                fieldId, allocId, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("400.0000"), IrrigationMethod.FURROW, "Plan 1"
        );
        MvcResult plan1Res = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(plan1Req)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID plan1Id = UUID.fromString(objectMapper.readTree(plan1Res.getResponse().getContentAsString()).get("data").get("id").asText());

        // 1. Event A: COMPLETED (350 m³) -> SHOULD COUNT
        RecordCompletedIrrigationEventRequest completedEvent = new RecordCompletedIrrigationEventRequest(
                fieldId, plan1Id, Instant.now().minus(4, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS),
                new BigDecimal("350.0000"), "Completed watering"
        );
        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completedEvent)))
                .andExpect(status().isCreated());

        // 2. Event B: STARTED (in-progress) -> MUST NOT COUNT
        CreateIrrigationEventRequest startedEvent = new CreateIrrigationEventRequest(
                fieldId, plan1Id, Instant.now(), "Still pumping"
        );
        mockMvc.perform(post("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startedEvent)))
                .andExpect(status().isCreated());

        // 3. Event C: STARTED then CANCELLED -> MUST NOT COUNT
        CreateIrrigationEventRequest cancelCandidate = new CreateIrrigationEventRequest(
                fieldId, plan1Id, Instant.now().minus(2, ChronoUnit.HOURS), "Will abort"
        );
        MvcResult cancelRes = mockMvc.perform(post("/api/v1/irrigation-events")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelCandidate)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID eventCId = UUID.fromString(objectMapper.readTree(cancelRes.getResponse().getContentAsString()).get("data").get("id").asText());
        mockMvc.perform(patch("/api/v1/irrigation-events/" + eventCId + "/cancel")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelOperationRequest("Aborted"))))
                .andExpect(status().isOk());

        // Verify Consumption Summary on GET /api/v1/water/allocations/{id}/summary
        mockMvc.perform(get("/api/v1/water/allocations/" + allocId + "/summary")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allocationId").value(allocId.toString()))
                .andExpect(jsonPath("$.data.allocatedVolumeM3").value(1000.0000))
                .andExpect(jsonPath("$.data.consumedVolumeM3").value(350.0000))
                .andExpect(jsonPath("$.data.remainingVolumeM3").value(650.0000))
                .andExpect(jsonPath("$.data.consumptionPercentage").value(35.00))
                .andExpect(jsonPath("$.data.isOverConsumed").value(false))
                .andExpect(jsonPath("$.data.overConsumedVolumeM3").value(0.0000))
                .andExpect(jsonPath("$.data.completedEventsCount").value(1));

        // Verify also on GET /api/v1/water-allocations/{id}/consumption
        mockMvc.perform(get("/api/v1/water-allocations/" + allocId + "/consumption")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allocationId").value(allocId.toString()))
                .andExpect(jsonPath("$.data.consumedVolumeM3").value(350.0000));
    }

    // =========================================================================
    // 2. Over-consumption Policy: Remaining Clamped to 0, Over-consumption Flagged
    // =========================================================================
    @Test
    @DisplayName("2. Over-consumption: Clamps remaining volume to 0, calculates excess volume, marks isOverConsumed=true")
    void testOverConsumptionDetection() throws Exception {
        String farmerToken = registerFarmer("Sobir", "OverCons");
        String operatorToken = createPrivilegedUser("opOverCons", UserRole.OPERATOR);

        UUID fieldId = createTestField(farmerToken, "Over-Consumption Field");
        UUID sourceId = createWaterSource(operatorToken, "Deep Well " + System.currentTimeMillis(), WaterSourceType.WELL);

        // Quota = 500 m³
        CreateWaterAllocationRequest allocReq = new CreateWaterAllocationRequest(
                fieldId, sourceId, new BigDecimal("500.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "Tight quota"
        );
        MvcResult allocRes = mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID allocId = UUID.fromString(objectMapper.readTree(allocRes.getResponse().getContentAsString()).get("data").get("id").asText());

        CreateIrrigationPlanRequest planReq = new CreateIrrigationPlanRequest(
                fieldId, allocId, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(8, ChronoUnit.HOURS),
                new BigDecimal("500.0000"), IrrigationMethod.DRIP, "Drip Plan"
        );
        MvcResult planRes = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID planId = UUID.fromString(objectMapper.readTree(planRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Event 1: 300 m³
        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordCompletedIrrigationEventRequest(
                                fieldId, planId, Instant.now().minus(10, ChronoUnit.HOURS), Instant.now().minus(6, ChronoUnit.HOURS),
                                new BigDecimal("300.0000"), "Delivery 1"
                        ))))
                .andExpect(status().isCreated());

        // Event 2: 320 m³ -> Total = 620 m³ (over 500 m³ quota by 120 m³)
        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordCompletedIrrigationEventRequest(
                                fieldId, planId, Instant.now().minus(5, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS),
                                new BigDecimal("320.0000"), "Delivery 2 - excess flow"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/water/allocations/" + allocId + "/summary")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allocatedVolumeM3").value(500.0000))
                .andExpect(jsonPath("$.data.consumedVolumeM3").value(620.0000))
                .andExpect(jsonPath("$.data.remainingVolumeM3").value(0.0000))
                .andExpect(jsonPath("$.data.isOverConsumed").value(true))
                .andExpect(jsonPath("$.data.overConsumedVolumeM3").value(120.0000))
                .andExpect(jsonPath("$.data.consumptionPercentage").value(124.00))
                .andExpect(jsonPath("$.data.completedEventsCount").value(2));
    }

    // =========================================================================
    // 3. Planned vs Actual Metrics on Irrigation Plan
    // =========================================================================
    @Test
    @DisplayName("3. Planned vs Actual: Accurately computes variance, percentage deviation, and consumption ratio")
    void testPlannedVsActualMetrics() throws Exception {
        String farmerToken = registerFarmer("Mansur", "PlanMetrics");
        UUID fieldId = createTestField(farmerToken, "Metrics Plan Field");

        // Scheduled target = 400 m³
        CreateIrrigationPlanRequest planReq = new CreateIrrigationPlanRequest(
                fieldId, null, Instant.now().plus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS),
                new BigDecimal("400.0000"), IrrigationMethod.SPRINKLER, "Sprinkler schedule"
        );
        MvcResult planRes = mockMvc.perform(post("/api/v1/irrigation-plans")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID planId = UUID.fromString(objectMapper.readTree(planRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Actual delivery = 380 m³ (saved 20 m³)
        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordCompletedIrrigationEventRequest(
                                fieldId, planId, Instant.now().minus(3, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS),
                                new BigDecimal("380.0000"), "Sprinkler pass"
                        ))))
                .andExpect(status().isCreated());

        // Test GET /api/v1/water/plans/{id}/metrics
        mockMvc.perform(get("/api/v1/water/plans/" + planId + "/metrics")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(planId.toString()))
                .andExpect(jsonPath("$.data.plannedVolumeM3").value(400.0000))
                .andExpect(jsonPath("$.data.actualVolumeM3").value(380.0000))
                .andExpect(jsonPath("$.data.varianceVolumeM3").value(-20.0000))
                .andExpect(jsonPath("$.data.absoluteVarianceVolumeM3").value(20.0000))
                .andExpect(jsonPath("$.data.variancePercentage").value(-5.00))
                .andExpect(jsonPath("$.data.consumptionRatioPercentage").value(95.00))
                .andExpect(jsonPath("$.data.completedEventsCount").value(1));

        // Test GET /api/v1/irrigation-plans/{id}/metrics
        mockMvc.perform(get("/api/v1/irrigation-plans/" + planId + "/metrics")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(planId.toString()))
                .andExpect(jsonPath("$.data.actualVolumeM3").value(380.0000));
    }

    // =========================================================================
    // 4. Field Water Summary: Aggregation and Period Filtering
    // =========================================================================
    @Test
    @DisplayName("4. Field Water Summary: Aggregates total allocations and deliveries with temporal filtering")
    void testFieldWaterSummaryWithPeriodFiltering() throws Exception {
        String farmerToken = registerFarmer("Nodir", "FieldSummary");
        String operatorToken = createPrivilegedUser("opFieldSum", UserRole.OPERATOR);

        UUID fieldId = createTestField(farmerToken, "Summary Multi Parcel");
        UUID sourceId = createWaterSource(operatorToken, "Main Branch " + System.currentTimeMillis(), WaterSourceType.CANAL);

        Instant t0 = Instant.now().minus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Instant t1 = t0.plus(10, ChronoUnit.DAYS);
        Instant t2 = t0.plus(20, ChronoUnit.DAYS);
        Instant t3 = t0.plus(40, ChronoUnit.DAYS);

        // Allocation 1: valid [t0, t2], 1500 m³
        mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWaterAllocationRequest(
                                fieldId, sourceId, new BigDecimal("1500.0000"), t0, t2, "Quota May"
                        ))))
                .andExpect(status().isCreated());

        // Allocation 2: valid [t2, t3], 2000 m³
        mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWaterAllocationRequest(
                                fieldId, sourceId, new BigDecimal("2000.0000"), t2, t3, "Quota June"
                        ))))
                .andExpect(status().isCreated());

        // Event 1: at t1 (450 m³)
        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordCompletedIrrigationEventRequest(
                                fieldId, null, t1, t1.plus(4, ChronoUnit.HOURS), new BigDecimal("450.0000"), "Event at t1"
                        ))))
                .andExpect(status().isCreated());

        // Event 2: at t2.plus(2 days) (600 m³)
        Instant t2Plus = t2.plus(2, ChronoUnit.DAYS);
        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordCompletedIrrigationEventRequest(
                                fieldId, null, t2Plus, t2Plus.plus(5, ChronoUnit.HOURS), new BigDecimal("600.0000"), "Event at t2Plus"
                        ))))
                .andExpect(status().isCreated());

        // 4.1 Unbounded Field Summary: All allocations (3500 m³) and all events (1050 m³)
        mockMvc.perform(get("/api/v1/water/fields/" + fieldId + "/summary")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.totalAllocatedVolumeM3").value(3500.0000))
                .andExpect(jsonPath("$.data.totalConsumedVolumeM3").value(1050.0000))
                .andExpect(jsonPath("$.data.totalRemainingVolumeM3").value(2450.0000))
                .andExpect(jsonPath("$.data.allocationCount").value(2))
                .andExpect(jsonPath("$.data.irrigationEventCount").value(2));

        // 4.2 Bounded Field Summary: from t0 to t1.plus(1 day) -> captures Allocation 1 (1500 m³) and Event 1 (450 m³) only
        mockMvc.perform(get("/api/v1/water/fields/" + fieldId + "/summary")
                        .header("Authorization", "Bearer " + farmerToken)
                        .param("from", t0.toString())
                        .param("to", t1.plus(1, ChronoUnit.DAYS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAllocatedVolumeM3").value(1500.0000))
                .andExpect(jsonPath("$.data.totalConsumedVolumeM3").value(450.0000))
                .andExpect(jsonPath("$.data.totalRemainingVolumeM3").value(1050.0000))
                .andExpect(jsonPath("$.data.allocationCount").value(1))
                .andExpect(jsonPath("$.data.irrigationEventCount").value(1));
    }

    // =========================================================================
    // 5. Aggregate Isolation: Events from other fields do NOT mix
    // =========================================================================
    @Test
    @DisplayName("5. Parcel Isolation: Consumption queries for Field 1 strictly exclude events from Field 2")
    void testFieldIsolationInConsumption() throws Exception {
        String farmerToken = registerFarmer("Qodir", "IsolationTest");
        String operatorToken = createPrivilegedUser("opIsoTest", UserRole.OPERATOR);

        UUID field1Id = createTestField(farmerToken, "Field Isolation One");
        UUID field2Id = createTestField(farmerToken, "Field Isolation Two");
        UUID sourceId = createWaterSource(operatorToken, "Isolation Feeder " + System.currentTimeMillis(), WaterSourceType.CANAL);

        // Field 1: 1000 m³ allocation
        mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWaterAllocationRequest(
                                field1Id, sourceId, new BigDecimal("1000.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "F1 Quota"
                        ))))
                .andExpect(status().isCreated());

        // Field 2: 2000 m³ allocation + 900 m³ completed event
        mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWaterAllocationRequest(
                                field2Id, sourceId, new BigDecimal("2000.0000"), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "F2 Quota"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/irrigation-events/completed")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordCompletedIrrigationEventRequest(
                                field2Id, null, Instant.now().minus(2, ChronoUnit.HOURS), Instant.now(), new BigDecimal("900.0000"), "F2 event"
                        ))))
                .andExpect(status().isCreated());

        // Field 1 has 0 events -> consumed must be strictly 0, not contaminated by Field 2
        mockMvc.perform(get("/api/v1/water/fields/" + field1Id + "/summary")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(field1Id.toString()))
                .andExpect(jsonPath("$.data.totalAllocatedVolumeM3").value(1000.0000))
                .andExpect(jsonPath("$.data.totalConsumedVolumeM3").value(0.0000))
                .andExpect(jsonPath("$.data.totalRemainingVolumeM3").value(1000.0000))
                .andExpect(jsonPath("$.data.allocationCount").value(1))
                .andExpect(jsonPath("$.data.irrigationEventCount").value(0));
    }
}
