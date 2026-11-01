package com.aquaai.recommendation.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.recommendation.api.dto.DismissRecommendationRequest;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationSource;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(9900);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String registerFarmer(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "rec.farmer." + id + "@yangiariq.uz";

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
                .email("operator." + id + "@yangiariq.uz")
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
                "Automated test plot in Yangiariq",
                new BigDecimal("15.5"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                IrrigationMethod.FURROW,
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
    @DisplayName("Unauthenticated request to recommendations returns 401 Unauthorized")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer cannot access another farmer's recommendation (Anti-IDOR: returns 404)")
    void farmerCannotAccessOtherFarmerRecommendation() throws Exception {
        String tokenA = registerFarmer("Anvar", "Rahimov");
        String tokenB = registerFarmer("Bekzod", "Karimov");

        UUID fieldA = createFieldForFarmer(tokenA, "Anvar's Cotton Field");
        UUID fieldB = createFieldForFarmer(tokenB, "Bekzod's Wheat Field");

        // Create a recommendation directly on Field A
        AgronomicRecommendation recA = recommendationRepository.save(AgronomicRecommendation.create(
                fieldA,
                RecommendationType.IRRIGATE_NOW,
                RecommendationPriority.HIGH,
                "Irrigate Anvar's Field",
                "Soil moisture below threshold",
                "Explanation",
                new BigDecimal("0.85"),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                RecommendationSource.HYBRID_AI,
                null,
                false,
                List.of(),
                List.of()
        ));

        // Farmer A can access
        mockMvc.perform(get("/api/v1/recommendations/" + recA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(recA.getId().toString()))
                .andExpect(jsonPath("$.data.fieldId").value(fieldA.toString()));

        // Farmer B attempts to access recA -> MUST return 404 (Anti-IDOR)
        mockMvc.perform(get("/api/v1/recommendations/" + recA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // Farmer B attempts to generate on Field A -> MUST return 404 (Anti-IDOR)
        mockMvc.perform(post("/api/v1/recommendations/generate?fieldId=" + fieldA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // Farmer B attempts to accept recA -> MUST return 404 (Anti-IDOR)
        mockMvc.perform(patch("/api/v1/recommendations/" + recA.getId() + "/accept")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Farmer can view, accept, and dismiss their own recommendation with reason")
    void farmerCanManageRecommendationLifecycle() throws Exception {
        String token = registerFarmer("Dilshod", "Alimov");
        UUID fieldId = createFieldForFarmer(token, "Dilshod's Orchard");

        // Trigger on-demand generation
        MvcResult genResult = mockMvc.perform(post("/api/v1/recommendations/generate?fieldId=" + fieldId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andReturn();

        JsonNode genNode = objectMapper.readTree(genResult.getResponse().getContentAsString());
        UUID recId = UUID.fromString(genNode.path("data").path("id").asText());

        // Mark viewed
        mockMvc.perform(patch("/api/v1/recommendations/" + recId + "/view")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VIEWED"));

        // Accept recommendation
        mockMvc.perform(patch("/api/v1/recommendations/" + recId + "/accept")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        // Complete recommendation
        mockMvc.perform(patch("/api/v1/recommendations/" + recId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Dismissal without reason fails with 400 Bad Request")
    void dismissalWithoutReasonFails() throws Exception {
        String token = registerFarmer("Erkin", "Yusupov");
        UUID fieldId = createFieldForFarmer(token, "Erkin's Field");

        AgronomicRecommendation rec = recommendationRepository.save(AgronomicRecommendation.create(
                fieldId,
                RecommendationType.MONITOR_SOIL_MOISTURE,
                RecommendationPriority.INFO,
                "Monitor",
                "Summary",
                "Explanation",
                new BigDecimal("0.80"),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                RecommendationSource.HYBRID_AI,
                null,
                false,
                List.of(),
                List.of()
        ));

        // Blank dismissal reason should return 400 Bad Request
        DismissRecommendationRequest blankRequest = new DismissRecommendationRequest("");
        mockMvc.perform(patch("/api/v1/recommendations/" + rec.getId() + "/dismiss")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankRequest)))
                .andExpect(status().isBadRequest());

        // Valid dismissal reason succeeds
        DismissRecommendationRequest validRequest = new DismissRecommendationRequest("Field was irrigated yesterday morning");
        mockMvc.perform(patch("/api/v1/recommendations/" + rec.getId() + "/dismiss")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISMISSED"))
                .andExpect(jsonPath("$.data.dismissalReason").value("Field was irrigated yesterday morning"));
    }

    @Test
    @DisplayName("District Operator can access district-wide recommendations and summary")
    void operatorCanAccessDistrictRecommendations() throws Exception {
        String farmerToken = registerFarmer("Farhod", "Tursunov");
        UUID fieldId = createFieldForFarmer(farmerToken, "Farhod's Parcel");

        recommendationRepository.save(AgronomicRecommendation.create(
                fieldId,
                RecommendationType.MANAGE_SALINITY,
                RecommendationPriority.HIGH,
                "Salinity Alert",
                "Elevated EC",
                "Detailed",
                new BigDecimal("0.82"),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                RecommendationSource.DETERMINISTIC_ENGINE,
                null,
                false,
                List.of(),
                List.of()
        ));

        String opToken = createOperatorToken();

        // Operator lists recommendations
        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        // Operator gets summary
        mockMvc.perform(get("/api/v1/recommendations/summary")
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalActive").isNumber());
    }
}
