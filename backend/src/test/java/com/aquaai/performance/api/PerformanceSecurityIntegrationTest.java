package com.aquaai.performance.api;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.performance.api.dto.CreateBaselineRequest;
import com.aquaai.performance.api.dto.CreateImpactAssessmentRequest;
import com.aquaai.performance.api.dto.CreateTargetRequest;
import com.aquaai.performance.api.dto.ReviewImpactAssessmentRequest;
import com.aquaai.performance.api.dto.RunEvaluationRequest;
import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.TargetCategory;
import com.aquaai.performance.domain.model.TargetType;
import com.aquaai.performance.domain.repository.ImpactAssessmentRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerformanceSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 9200000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private ImpactAssessmentRepository impactAssessmentRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String createToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
    }

    private User persistUser(UserRole role) {
        long id = USER_COUNTER.incrementAndGet();
        return userRepository.save(User.builder()
                .firstName("Test")
                .lastName("Performance")
                .fullName("Test Performance " + id)
                .phoneNumber("+99890" + String.format("%07d", id))
                .email("perf." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedPasswordTestingPerformancePlaceholder123456789")
                .role(role)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
    }

    private Field persistField(User owner) {
        return fieldRepository.save(Field.create(
                owner.getId(),
                "Farmer Field " + UUID.randomUUID().toString().substring(0, 6),
                "Parcel description",
                new BigDecimal("12.50"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                FieldLocation.of(41.38, 60.62),
                null
        ));
    }

    @Test
    @DisplayName("1. Unauthenticated request to /api/v1/performance/overview returns 401")
    void testUnauthenticatedOverview() throws Exception {
        mockMvc.perform(get("/api/v1/performance/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. Operator can access overview, indicators, and trends")
    void testOperatorReadOverviewAndIndicators() throws Exception {
        User operator = persistUser(UserRole.OPERATOR);
        String token = createToken(operator);

        mockMvc.perform(get("/api/v1/performance/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryFulfillmentPct").exists())
                .andExpect(jsonPath("$.data.totalWaterDeliveredEvidence").value("CALCULATED"));

        mockMvc.perform(get("/api/v1/performance/indicators")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/performance/trends")
                        .param("indicatorCode", "WDP_DELIVERY_FULFILLMENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.indicatorCode").value("WDP_DELIVERY_FULFILLMENT"))
                .andExpect(jsonPath("$.data.dataPoints").isArray());
    }

    @Test
    @DisplayName("3. Operator can create baseline and target; Farmer is denied (403 Forbidden)")
    void testBaselineAndTargetSecurity() throws Exception {
        User operator = persistUser(UserRole.OPERATOR);
        User farmer = persistUser(UserRole.FARMER);

        String opToken = createToken(operator);
        String farmerToken = createToken(farmer);

        Instant now = Instant.now();
        CreateBaselineRequest baseReq = CreateBaselineRequest.builder()
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .name("Q2 2026 Test Baseline")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeName("Yangiariq District")
                .periodStart(now.minus(45, ChronoUnit.DAYS))
                .periodEnd(now.minus(15, ChronoUnit.DAYS))
                .baselineValue(new BigDecimal("86.20"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .dataCompletenessPct(new BigDecimal("95.00"))
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .calculationMethod("HISTORICAL_AVERAGE")
                .seasonContext("MAIN_SEASON")
                .build();

        // Farmer attempt -> 403
        mockMvc.perform(post("/api/v1/performance/baselines")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseReq)))
                .andExpect(status().isForbidden());

        // Operator attempt -> 200 OK
        mockMvc.perform(post("/api/v1/performance/baselines")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Q2 2026 Test Baseline"));

        CreateTargetRequest targetReq = CreateTargetRequest.builder()
                .indicatorCode("CCP_LOSS_PERCENTAGE")
                .title("Q2 Canal Loss Limit")
                .scopeType(GeographicScopeType.CANAL_REACH)
                .scopeName("Boʻzyop Main Canal")
                .targetType(TargetType.MAXIMUM)
                .targetCategory(TargetCategory.OPERATIONAL_TARGET)
                .targetValue(new BigDecimal("8.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .effectiveStart(now)
                .targetOwner("Operator Office")
                .rationale("Lowering reach seepage")
                .build();

        // Farmer attempt -> 403
        mockMvc.perform(post("/api/v1/performance/targets")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetReq)))
                .andExpect(status().isForbidden());

        // Operator attempt -> 200 OK
        mockMvc.perform(post("/api/v1/performance/targets")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Q2 Canal Loss Limit"));
    }

    @Test
    @DisplayName("4. Operator can run performance evaluation and retrieve snapshot")
    void testRunEvaluation() throws Exception {
        User operator = persistUser(UserRole.OPERATOR);
        String opToken = createToken(operator);

        RunEvaluationRequest req = RunEvaluationRequest.builder()
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .scopeType(GeographicScopeType.DISTRICT)
                .periodStart(Instant.now().minus(30, ChronoUnit.DAYS))
                .periodEnd(Instant.now())
                .customValue(new BigDecimal("93.50"))
                .build();

        mockMvc.perform(post("/api/v1/performance/evaluations")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.calculatedValue").value(93.50))
                .andExpect(jsonPath("$.data.evaluationNumber").exists());
    }

    @Test
    @DisplayName("5. Impact assessment creation and review lifecycle")
    void testImpactAssessmentLifecycle() throws Exception {
        User operator = persistUser(UserRole.OPERATOR);
        String opToken = createToken(operator);

        Instant now = Instant.now();
        CreateImpactAssessmentRequest req = CreateImpactAssessmentRequest.builder()
                .title("Canal Lining Impact Test")
                .interventionType("CANAL_MAINTENANCE")
                .interventionId(UUID.randomUUID())
                .interventionReference("CA-TEST-009")
                .interventionDate(now.minus(20, ChronoUnit.DAYS))
                .indicatorCode("CCP_LOSS_PERCENTAGE")
                .scopeType(GeographicScopeType.CANAL_REACH)
                .scopeName("Boʻzyop Reach 2")
                .baselinePeriodStart(now.minus(50, ChronoUnit.DAYS))
                .baselinePeriodEnd(now.minus(20, ChronoUnit.DAYS))
                .baselineValue(new BigDecimal("12.00"))
                .evaluationPeriodStart(now.minus(20, ChronoUnit.DAYS))
                .evaluationPeriodEnd(now)
                .postInterventionValue(new BigDecimal("7.50"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .methodologyNotes("Acoustic gauge comparison across lined segment.")
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/performance/impact-assessments")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.changeDirection").value("FAVORABLE"))
                .andReturn().getResponse().getContentAsString();

        String assessmentId = objectMapper.readTree(responseJson).get("data").get("id").asText();

        // Review assessment
        ReviewImpactAssessmentRequest reviewReq = ReviewImpactAssessmentRequest.builder()
                .reviewStatus(AssessmentReviewStatus.VERIFIED_OUTCOME)
                .reviewNotes("Verified by field inspection.")
                .build();

        mockMvc.perform(post("/api/v1/performance/impact-assessments/" + assessmentId + "/review")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewStatus").value("VERIFIED_OUTCOME"))
                .andExpect(jsonPath("$.data.attributionLevel").value("DIRECTLY_VERIFIED"));
    }

    @Test
    @DisplayName("6. Anti-IDOR: Farmer attempting to query impact assessments for another farmer's field is rejected with 403 Forbidden")
    void testAntiIdorProtection() throws Exception {
        User farmerA = persistUser(UserRole.FARMER);
        User farmerB = persistUser(UserRole.FARMER);

        Field fieldB = persistField(farmerB); // Owned by farmer B

        String tokenA = createToken(farmerA);

        // Farmer A querying field B -> 403 Forbidden
        mockMvc.perform(get("/api/v1/performance/impact-assessments")
                        .param("scopeId", fieldB.getId().toString())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Operator can generate reproducible performance reports with disclaimer")
    void testGenerateReport() throws Exception {
        User operator = persistUser(UserRole.OPERATOR);
        String opToken = createToken(operator);

        mockMvc.perform(get("/api/v1/performance/reports")
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").exists())
                .andExpect(jsonPath("$.data.legalDisclaimer").exists())
                .andExpect(jsonPath("$.data.overview").exists())
                .andExpect(jsonPath("$.data.evaluations").isArray());
    }
}
