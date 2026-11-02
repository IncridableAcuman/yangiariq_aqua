package com.aquaai.operations.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.operations.api.dto.ApproveOperationalPlanRequest;
import com.aquaai.operations.api.dto.CompleteOperationRequest;
import com.aquaai.operations.api.dto.PreparePlanRequest;
import com.aquaai.operations.api.dto.RejectOperationalPlanRequest;
import com.aquaai.operations.api.dto.ScheduleOperationalPlanRequest;
import com.aquaai.operations.api.dto.StartOperationRequest;
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
import java.time.LocalDate;
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
class OperationsWorkflowIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(40000);

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
        String email = "farmer." + id + "@operations.yangiariq.uz";

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
        String email = "operator." + id + "@operations.yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("Operations")
                .fullName("Operator Operations")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private String createField(String token, String name, CropType cropType) throws Exception {
        double lat = 41.34;
        double lon = 60.59;
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(lon, lat),
                CoordinatePoint.of(lon + 0.01, lat),
                CoordinatePoint.of(lon + 0.01, lat + 0.01),
                CoordinatePoint.of(lon, lat + 0.01),
                CoordinatePoint.of(lon, lat)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                name,
                "Operations test field",
                new BigDecimal("10.5"),
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

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return data.get("id").asText();
    }

    @Test
    @DisplayName("Complete Decision-to-Action Operational Flow: Context -> Prepare -> Approve -> Schedule -> Start -> Complete -> Verification")
    void completeDecisionToActionFlow() throws Exception {
        String farmerToken = registerFarmer("Anvar", "Rashidov");
        String operatorToken = createOperatorToken();
        String fieldId = createField(farmerToken, "Dala 01 Paxta", CropType.COTTON);

        // Create water source & allocation to provide real seasonal water quota
        String sourceName = "Test Canal " + System.currentTimeMillis();
        MvcResult sourceRes = mockMvc.perform(post("/api/v1/water-sources")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.aquaai.water.api.dto.CreateWaterSourceRequest(
                                sourceName, com.aquaai.water.domain.model.WaterSourceType.CANAL, "Intake canal"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        String sourceId = objectMapper.readTree(sourceRes.getResponse().getContentAsString()).get("data").get("id").asText();

        Instant periodStart = Instant.now().minusSeconds(86400);
        Instant periodEnd = Instant.now().plusSeconds(86400 * 90);
        com.aquaai.water.api.dto.CreateWaterAllocationRequest allocReq = new com.aquaai.water.api.dto.CreateWaterAllocationRequest(
                UUID.fromString(fieldId),
                UUID.fromString(sourceId),
                new BigDecimal("5000.0000"),
                periodStart,
                periodEnd,
                "Operations season quota"
        );
        mockMvc.perform(post("/api/v1/water-allocations")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated());

        // 1. Get Operational Context
        MvcResult contextResult = mockMvc.perform(get("/api/v1/operations/" + fieldId + "/context")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.field.id").value(fieldId))
                .andExpect(jsonPath("$.data.field.name").value("Dala 01 Paxta"))
                .andExpect(jsonPath("$.data.workflow.currentState").exists())
                .andExpect(jsonPath("$.data.workflow.timeline").isArray())
                .andReturn();

        JsonNode contextData = objectMapper.readTree(contextResult.getResponse().getContentAsString()).get("data");
        assertThat(contextData.get("workflow").get("timeline").size()).isEqualTo(7);

        // 2. Prepare Plan
        PreparePlanRequest prepareReq = PreparePlanRequest.builder()
                .targetDate(LocalDate.now())
                .customVolumeM3(BigDecimal.valueOf(420.0))
                .customNotes("Prepared for cotton flowering")
                .build();

        MvcResult prepareResult = mockMvc.perform(post("/api/v1/operations/" + fieldId + "/prepare-plan")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prepareReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId))
                .andExpect(jsonPath("$.data.status").value("PROPOSED"))
                .andReturn();

        String planId = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // 3. Human Approval - Validation failure without acknowledgement checkbox
        ApproveOperationalPlanRequest unacknowledgedReq = ApproveOperationalPlanRequest.builder()
                .acknowledgeConstraints(false)
                .build();

        mockMvc.perform(post("/api/v1/operations/plans/" + planId + "/approve")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unacknowledgedReq)))
                .andExpect(status().isBadRequest());

        // 3b. Human Approval - Success with mandatory acknowledgement checkbox
        ApproveOperationalPlanRequest approveReq = ApproveOperationalPlanRequest.builder()
                .acknowledgeConstraints(true)
                .approvalNotes("Approved after reviewing soil moisture deficit")
                .build();

        mockMvc.perform(post("/api/v1/operations/plans/" + planId + "/approve")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(planId))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvedBy").isNotEmpty())
                .andExpect(jsonPath("$.data.approvedAt").isNotEmpty());

        // 4. Schedule Operation
        ScheduleOperationalPlanRequest scheduleReq = ScheduleOperationalPlanRequest.builder()
                .scheduledStartAt(Instant.now().plusSeconds(3600))
                .scheduledEndAt(Instant.now().plusSeconds(7200))
                .dispatchNotes("Nocturnal canal turn scheduled")
                .build();

        mockMvc.perform(post("/api/v1/operations/plans/" + planId + "/schedule")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        // 5. Start Operation (Manual commencement recording)
        StartOperationRequest startReq = StartOperationRequest.builder()
                .startedAt(Instant.now())
                .operatorNotes("Manually initiated canal intake valve")
                .build();

        mockMvc.perform(post("/api/v1/operations/plans/" + planId + "/start")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 6. Complete Operation (Manual delivery completion recording)
        CompleteOperationRequest completeReq = CompleteOperationRequest.builder()
                .actualVolumeDeliveredM3(BigDecimal.valueOf(420.0))
                .endedAt(Instant.now().plusSeconds(3600))
                .operatorNotes("Completed irrigation delivery manually")
                .build();

        mockMvc.perform(post("/api/v1/operations/plans/" + planId + "/complete")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // 7. Post-Irrigation Verification
        mockMvc.perform(get("/api/v1/operations/plans/" + planId + "/verification")
                        .header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(planId))
                .andExpect(jsonPath("$.data.plannedVolumeM3").isNumber())
                .andExpect(jsonPath("$.data.actualVolumeM3").isNumber())
                .andExpect(jsonPath("$.data.isRechargeSuccessful").value(true))
                .andExpect(jsonPath("$.data.verificationSummary").isNotEmpty())
                .andExpect(jsonPath("$.data.learningEvidence").isNotEmpty());
    }

    @Test
    @DisplayName("Anti-IDOR Security: Farmer cannot access or mutate foreign field operations (404 anti-enumeration)")
    void enforcesAntiIdorForForeignField() throws Exception {
        String farmerA = registerFarmer("FarmerA", "Aliyev");
        String farmerB = registerFarmer("FarmerB", "Valiyev");

        String fieldAId = createField(farmerA, "Field A", CropType.WHEAT);

        // Farmer B attempts to get context of Field A -> 404 Not Found
        mockMvc.perform(get("/api/v1/operations/" + fieldAId + "/context")
                        .header("Authorization", "Bearer " + farmerB))
                .andExpect(status().isNotFound());

        // Farmer B attempts to prepare plan for Field A -> 404 Not Found
        PreparePlanRequest req = PreparePlanRequest.builder().build();
        mockMvc.perform(post("/api/v1/operations/" + fieldAId + "/prepare-plan")
                        .header("Authorization", "Bearer " + farmerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        // Unauthenticated request -> 401 Unauthorized
        mockMvc.perform(get("/api/v1/operations/" + fieldAId + "/context"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Operator has authorized district scope across parcels")
    void operatorHasDistrictScopeAccess() throws Exception {
        String farmer = registerFarmer("DistrictFarmer", "Karimov");
        String fieldId = createField(farmer, "District Field", CropType.COTTON);

        String operatorToken = createOperatorToken();

        // Operator gets context of parcel in district -> 200 OK
        mockMvc.perform(get("/api/v1/operations/" + fieldId + "/context")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.field.id").value(fieldId));
    }

    @Test
    @DisplayName("Reject Plan: Successfully cancels plan with mandatory reason")
    void rejectPlanRequiresReason() throws Exception {
        String farmerToken = registerFarmer("Sardor", "Nematov");
        String fieldId = createField(farmerToken, "Reject Test Field", CropType.MAIZE);

        // Prepare plan
        MvcResult prepareResult = mockMvc.perform(post("/api/v1/operations/" + fieldId + "/prepare-plan")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn();

        String planId = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // Reject with empty reason -> 400 Bad Request
        mockMvc.perform(post("/api/v1/operations/plans/" + planId + "/reject")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"\"}"))
                .andExpect(status().isBadRequest());

        // Reject with valid reason -> 200 OK
        RejectOperationalPlanRequest rejectReq = RejectOperationalPlanRequest.builder()
                .reason("Rainfall expected tomorrow; irrigation unnecessary")
                .build();

        mockMvc.perform(post("/api/v1/operations/plans/" + planId + "/reject")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancellationReason").value("Rainfall expected tomorrow; irrigation unnecessary"));
    }
}
