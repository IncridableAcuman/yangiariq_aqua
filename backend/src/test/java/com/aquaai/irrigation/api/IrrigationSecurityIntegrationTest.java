package com.aquaai.irrigation.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.irrigation.api.dto.ApprovePlanRequest;
import com.aquaai.irrigation.api.dto.CancelPlanRequest;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
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
class IrrigationSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(8800);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmartIrrigationPlanRepository planRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String registerFarmer(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "plan.farmer." + id + "@yangiariq.uz";

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
        User op = userRepository.save(User.builder()
                .firstName("District")
                .lastName("Operator")
                .fullName("District Operator")
                .phoneNumber("+99893" + String.format("%07d", id))
                .email("operator.plan." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForOperator123456789")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(op.getId(), op.getPhoneNumber(), op.getRole());
    }

    private UUID createFieldForFarmer(String token, String name) throws Exception {
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(60.61, 41.34),
                CoordinatePoint.of(60.62, 41.34),
                CoordinatePoint.of(60.62, 41.35),
                CoordinatePoint.of(60.61, 41.35),
                CoordinatePoint.of(60.61, 41.34)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                name,
                "Automated test plot in Yangiariq for smart irrigation",
                new BigDecimal("12.5"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                new BigDecimal("41.345000"),
                new BigDecimal("60.615000"),
                boundary
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(root.path("data").path("id").asText());
    }

    @Test
    @DisplayName("Unauthenticated request to irrigation plans returns 401 Unauthorized")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/irrigation/plans"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer can generate and retrieve smart irrigation plan for their own field")
    void farmerCanGenerateAndRetrievePlanForOwnField() throws Exception {
        String token = registerFarmer("Anvar", "Karimov");
        UUID fieldId = createFieldForFarmer(token, "Anvar Cotton Plot");

        MvcResult generateResult = mockMvc.perform(post("/api/v1/fields/" + fieldId + "/irrigation/plans/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.status").value("PROPOSED"))
                .andReturn();

        JsonNode root = objectMapper.readTree(generateResult.getResponse().getContentAsString());
        UUID planId = UUID.fromString(root.path("data").path("id").asText());

        mockMvc.perform(get("/api/v1/irrigation/plans/" + planId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(planId.toString()));
    }

    @Test
    @DisplayName("Anti-IDOR: Farmer cannot access, generate, or approve plan for another farmer's field (HTTP 404)")
    void antiIdorFarmerCannotAccessForeignPlanReturns404() throws Exception {
        String farmer1Token = registerFarmer("Bobur", "Aliyev");
        String farmer2Token = registerFarmer("Sardor", "Qodirov");

        UUID farmer1FieldId = createFieldForFarmer(farmer1Token, "Bobur Wheat Field");

        // Farmer 2 attempts to generate a plan for Farmer 1's field -> 404 Not Found
        mockMvc.perform(post("/api/v1/fields/" + farmer1FieldId + "/irrigation/plans/generate")
                        .header("Authorization", "Bearer " + farmer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        // Farmer 1 generates a plan
        MvcResult res = mockMvc.perform(post("/api/v1/fields/" + farmer1FieldId + "/irrigation/plans/generate")
                        .header("Authorization", "Bearer " + farmer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn();

        UUID planId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asText());

        // Farmer 2 attempts to view Farmer 1's plan -> 404 Not Found (anti-enumeration)
        mockMvc.perform(get("/api/v1/irrigation/plans/" + planId)
                        .header("Authorization", "Bearer " + farmer2Token))
                .andExpect(status().isNotFound());

        // Farmer 2 attempts to approve Farmer 1's plan -> 404 Not Found
        mockMvc.perform(post("/api/v1/irrigation/plans/" + planId + "/approve")
                        .header("Authorization", "Bearer " + farmer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"acknowledgeWarnings\": true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Farmer can approve their own plan and then cancel it with a valid reason")
    void farmerCanApproveAndCancelPlan() throws Exception {
        String token = registerFarmer("Dilshod", "Rahimov");
        UUID fieldId = createFieldForFarmer(token, "Dilshod Melon Field");

        MvcResult genRes = mockMvc.perform(post("/api/v1/fields/" + fieldId + "/irrigation/plans/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn();

        UUID planId = UUID.fromString(objectMapper.readTree(genRes.getResponse().getContentAsString()).path("data").path("id").asText());

        // Approve with acknowledgeWarnings = true
        ApprovePlanRequest approveReq = new ApprovePlanRequest();
        approveReq.setAcknowledgeWarnings(true);

        mockMvc.perform(post("/api/v1/irrigation/plans/" + planId + "/approve")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Cancel with reason
        CancelPlanRequest cancelReq = new CancelPlanRequest();
        cancelReq.setReason("Canal maintenance scheduled by water authority");

        mockMvc.perform(post("/api/v1/irrigation/plans/" + planId + "/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancellationReason").value("Canal maintenance scheduled by water authority"));
    }

    @Test
    @DisplayName("District Operator can view all irrigation plans and water budgets")
    void operatorCanViewPlansAndWaterBudget() throws Exception {
        String farmerToken = registerFarmer("Jasur", "Nazarov");
        String operatorToken = createOperatorToken();

        UUID fieldId = createFieldForFarmer(farmerToken, "Jasur Rice Field");

        mockMvc.perform(post("/api/v1/fields/" + fieldId + "/irrigation/plans/generate")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());

        // Operator has district access to list and water-budget
        mockMvc.perform(get("/api/v1/irrigation/plans")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/water-budget")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));
    }
}
