package com.aquaai.risk.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.risk.api.dto.AcknowledgeAssessmentRequest;
import com.aquaai.risk.api.dto.ResolveAssessmentRequest;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.model.RiskTrend;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
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
class RiskIntelligenceSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 8500000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    private String registerFarmer(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "risk.farmer." + id + "." + UUID.randomUUID().toString().substring(0, 5) + "@yangiariq.uz";

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
                .firstName("Risk")
                .lastName("Operator")
                .fullName("Risk District Operator")
                .phoneNumber("+99893" + String.format("%07d", id))
                .email("risk.operator." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForOperator123456789")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(op.getId(), op.getPhoneNumber(), op.getRole());
    }

    private String createAdminToken() {
        long id = USER_COUNTER.incrementAndGet();
        User admin = userRepository.save(User.builder()
                .firstName("Risk")
                .lastName("Admin")
                .fullName("Risk Admin")
                .phoneNumber("+99894" + String.format("%07d", id))
                .email("risk.admin." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForAdmin123456789")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(admin.getId(), admin.getPhoneNumber(), admin.getRole());
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
                "Automated test plot in Yangiariq for risk intelligence evaluation",
                new BigDecimal("12.5"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
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

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asText());
    }

    @Test
    @DisplayName("1. Unauthenticated requests to risk intelligence are rejected with 401 Unauthorized")
    void unauthenticatedAccessShouldBeRejected() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/risk-intelligence/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/risk-intelligence/fields"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/risk-intelligence/fields/" + randomId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/risk-intelligence/recommendations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/risk-intelligence/assessments/" + randomId + "/acknowledge"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/risk-intelligence/assessments/" + randomId + "/resolve"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. Farmer cannot access another farmer's field risk profile - Anti-IDOR returns 404 Not Found")
    void farmerCannotAccessForeignFieldRisk() throws Exception {
        String farmerAToken = registerFarmer("Temur", "Rahimov");
        String farmerBToken = registerFarmer("Otabek", "Yuldashev");

        UUID fieldA = createFieldForFarmer(farmerAToken, "Temur's Cotton Parcel");
        UUID fieldB = createFieldForFarmer(farmerBToken, "Otabek's Wheat Parcel");

        // Farmer A querying Farmer B's field risk profile -> 404 (anti-enumeration)
        mockMvc.perform(get("/api/v1/risk-intelligence/fields/" + fieldB)
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isNotFound());

        // Farmer A querying Farmer B's field risk history -> 404
        mockMvc.perform(get("/api/v1/risk-intelligence/fields/" + fieldB + "/history")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("3. Farmer can inspect and query risk profile for own field")
    void farmerCanAccessOwnFieldRisk() throws Exception {
        String token = registerFarmer("Sanjar", "Ergashev");
        UUID fieldId = createFieldForFarmer(token, "Sanjar's Drip Field");

        // Query field risk profile
        mockMvc.perform(get("/api/v1/risk-intelligence/fields/" + fieldId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.fieldName").value("Sanjar's Drip Field"))
                .andExpect(jsonPath("$.data.overallSeverity").exists())
                .andExpect(jsonPath("$.data.overallConfidence").exists())
                .andExpect(jsonPath("$.data.riskScore").exists())
                .andExpect(jsonPath("$.data.priorityRank").exists())
                .andExpect(jsonPath("$.data.priorityReason").exists());

        // Overview endpoint
        mockMvc.perform(get("/api/v1/risk-intelligence/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAssessedFields").isNumber())
                .andExpect(jsonPath("$.data.categoryDistribution").isMap());
    }

    @Test
    @DisplayName("4. Farmer cannot acknowledge or resolve risk assessment belonging to another farmer")
    void farmerCannotAcknowledgeOrResolveForeignAssessment() throws Exception {
        String farmerAToken = registerFarmer("Nodir", "Aliev");
        String farmerBToken = registerFarmer("Farhod", "Rustamov");

        UUID fieldB = createFieldForFarmer(farmerBToken, "Farhod's Parcel");

        // Create an assessment directly on Farmer B's field
        FarmRiskAssessment assessmentB = riskAssessmentRepository.save(FarmRiskAssessment.builder()
                .fieldId(fieldB)
                .fieldName("Farhod's Parcel")
                .overallSeverity(RiskSeverity.HIGH)
                .overallConfidence(RiskConfidence.HIGH)
                .riskTrend(RiskTrend.STABLE)
                .riskScore(new BigDecimal("65.00"))
                .isHydraulicBlocked(false)
                .primaryRiskCategory(RiskCategory.SOIL_MOISTURE)
                .lifecycleStatus(RiskAssessmentLifecycleStatus.OPEN)
                .priorityRank(5)
                .priorityReason("Moisture deficit detected")
                .riskFactors(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .missingDataWarnings(Collections.emptyList())
                .evaluatedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        // Farmer A attempts to acknowledge Farmer B's assessment -> 404 Not Found
        AcknowledgeAssessmentRequest ackReq = new AcknowledgeAssessmentRequest("Acknowledged");
        mockMvc.perform(post("/api/v1/risk-intelligence/assessments/" + assessmentB.getId() + "/acknowledge")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ackReq)))
                .andExpect(status().isNotFound());

        // Farmer A attempts to resolve Farmer B's assessment -> 404 Not Found
        ResolveAssessmentRequest resReq = new ResolveAssessmentRequest("Resolved issue");
        mockMvc.perform(post("/api/v1/risk-intelligence/assessments/" + assessmentB.getId() + "/resolve")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5. Farmer can acknowledge and resolve risk assessment on own field")
    void farmerCanAcknowledgeAndResolveOwnAssessment() throws Exception {
        String token = registerFarmer("Botir", "Sobirov");
        UUID fieldId = createFieldForFarmer(token, "Botir's Cotton Stand");

        FarmRiskAssessment assessment = riskAssessmentRepository.save(FarmRiskAssessment.builder()
                .fieldId(fieldId)
                .fieldName("Botir's Cotton Stand")
                .overallSeverity(RiskSeverity.HIGH)
                .overallConfidence(RiskConfidence.HIGH)
                .riskTrend(RiskTrend.STABLE)
                .riskScore(new BigDecimal("60.00"))
                .isHydraulicBlocked(false)
                .primaryRiskCategory(RiskCategory.SOIL_MOISTURE)
                .lifecycleStatus(RiskAssessmentLifecycleStatus.OPEN)
                .priorityRank(5)
                .priorityReason("Moisture deficit detected")
                .riskFactors(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .missingDataWarnings(Collections.emptyList())
                .evaluatedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        // 1. Acknowledge
        AcknowledgeAssessmentRequest ackReq = new AcknowledgeAssessmentRequest("Farmer acknowledged risk");
        mockMvc.perform(post("/api/v1/risk-intelligence/assessments/" + assessment.getId() + "/acknowledge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ackReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lifecycleStatus").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.data.acknowledgedAt").exists());

        // 2. Resolve
        ResolveAssessmentRequest resReq = new ResolveAssessmentRequest("Drip irrigation executed successfully");
        mockMvc.perform(post("/api/v1/risk-intelligence/assessments/" + assessment.getId() + "/resolve")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lifecycleStatus").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolvedAt").exists())
                .andExpect(jsonPath("$.data.resolutionNotes").value("Drip irrigation executed successfully"));
    }

    @Test
    @DisplayName("6. District Operator and Admin have authorized district-wide access to risk intelligence")
    void operatorAndAdminHaveDistrictAccess() throws Exception {
        String farmerToken = registerFarmer("Mansur", "Karimov");
        UUID fieldId = createFieldForFarmer(farmerToken, "Mansur's Orchard");

        String opToken = createOperatorToken();
        String adminToken = createAdminToken();

        // Operator access to parcel profile
        mockMvc.perform(get("/api/v1/risk-intelligence/fields/" + fieldId)
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));

        // Admin access to parcel profile
        mockMvc.perform(get("/api/v1/risk-intelligence/fields/" + fieldId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));

        // Operator overview
        mockMvc.perform(get("/api/v1/risk-intelligence/overview")
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk());

        // Admin recommendations
        mockMvc.perform(get("/api/v1/risk-intelligence/recommendations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
