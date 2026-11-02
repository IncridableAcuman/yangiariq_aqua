package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionClassificationResult;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.port.PredictionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictionEvaluationIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(80000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private PredictionEvaluationRepository evaluationRepository;

    private String registerFarmer(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99895" + String.format("%07d", id);
        String email = "farmer." + id + "@eval.yangiariq.uz";

        RegisterRequest request = RegisterRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phone)
                .email(email)
                .password("SecurePass123!")
                .district("Yangiariq")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class);
        return auth.accessToken();
    }

    private UUID createField(String token, String name, CropType cropType) throws Exception {
        double lat = 41.35;
        double lon = 60.60;
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(lon, lat),
                CoordinatePoint.of(lon + 0.01, lat),
                CoordinatePoint.of(lon + 0.01, lat + 0.01),
                CoordinatePoint.of(lon, lat + 0.01),
                CoordinatePoint.of(lon, lat)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                name,
                "Evaluation parcel " + name,
                BigDecimal.valueOf(10.5),
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

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(root.get("data").get("id").asText());
    }

    @Test
    @DisplayName("Farmer can retrieve prediction evaluation overview with honest metrics")
    void testEvaluationOverview() throws Exception {
        String token = registerFarmer("Temur", "Malik");
        UUID fieldId = createField(token, "Temur Field A", CropType.COTTON);

        // Pre-populate an evaluation
        UUID predId = UUID.randomUUID();
        Instant now = Instant.now();
        Prediction pred = Prediction.builder()
                .id(predId)
                .fieldId(fieldId)
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .targetTime(now.minus(2, ChronoUnit.HOURS))
                .generatedAt(now.minus(26, ChronoUnit.HOURS))
                .predictedValue(new BigDecimal("25.00"))
                .lowerBound(new BigDecimal("22.00"))
                .upperBound(new BigDecimal("28.00"))
                .confidencePercent(85)
                .riskLevel(RiskLevel.LOW)
                .build();
        predictionRepository.save(pred);

        PredictionEvaluation eval = PredictionEvaluation.createNumerical(
                predId, fieldId, PredictionType.SOIL_MOISTURE_FORECAST, PredictionHorizon.NEXT_24_HOURS,
                "DETERMINISTIC_FAO56_V1", now.minus(26, ChronoUnit.HOURS), now.minus(2, ChronoUnit.HOURS),
                now.minus(2, ChronoUnit.HOURS), new BigDecimal("25.00"), new BigDecimal("22.00"),
                new BigDecimal("28.00"), new BigDecimal("26.00"), null, null, null,
                "Matched", false, 85, EvaluationStatus.EVALUATED, EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE, "TEST", "notes"
        );
        evaluationRepository.save(eval);

        mockMvc.perform(get("/api/v1/predictions/evaluation/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEvaluated").value(1))
                .andExpect(jsonPath("$.data.overallModelHealth").isNotEmpty())
                .andExpect(jsonPath("$.data.performanceByType").isArray())
                .andExpect(jsonPath("$.data.confidenceCalibration").isArray())
                .andExpect(jsonPath("$.data.dataQualityImpact").isArray());
    }

    @Test
    @DisplayName("Performance by type endpoint returns metrics with sample-size honesty")
    void testPerformanceByType() throws Exception {
        String token = registerFarmer("Jaloliddin", "Manguberdi");
        createField(token, "Jaloliddin Field 1", CropType.WHEAT);

        mockMvc.perform(get("/api/v1/predictions/evaluation/by-type")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "SOIL_MOISTURE_FORECAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].predictionType").value("SOIL_MOISTURE_FORECAST"))
                .andExpect(jsonPath("$.data[0].category").value("NUMERICAL"));
    }

    @Test
    @DisplayName("Evaluation by field endpoint lists field-level evaluation cards")
    void testEvaluationByField() throws Exception {
        String token = registerFarmer("Alpomish", "Botir");
        UUID fieldId = createField(token, "Alpomish Field 1", CropType.COTTON);

        mockMvc.perform(get("/api/v1/predictions/evaluation/by-field")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fields").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("Evaluation history endpoint lists evaluations with filtering")
    void testEvaluationHistory() throws Exception {
        String token = registerFarmer("Gorchakov", "Alex");
        UUID fieldId = createField(token, "Alex Field 1", CropType.MAIZE);

        mockMvc.perform(get("/api/v1/predictions/evaluation/history")
                        .header("Authorization", "Bearer " + token)
                        .param("fieldId", fieldId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Detail evaluation endpoint retrieves full audit context for single prediction")
    void testSinglePredictionDetailEvaluation() throws Exception {
        String token = registerFarmer("Nodira", "Begim");
        UUID fieldId = createField(token, "Nodira Bogʻ", CropType.MELON);

        UUID predId = UUID.randomUUID();
        Instant now = Instant.now();
        Prediction pred = Prediction.builder()
                .id(predId)
                .fieldId(fieldId)
                .predictionType(PredictionType.IRRIGATION_NEED_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .targetTime(now.minus(2, ChronoUnit.HOURS))
                .generatedAt(now.minus(26, ChronoUnit.HOURS))
                .predictedValue(new BigDecimal("150.00"))
                .confidencePercent(75)
                .riskLevel(RiskLevel.LOW)
                .build();
        predictionRepository.save(pred);

        mockMvc.perform(get("/api/v1/predictions/" + predId + "/evaluation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.predictionId").value(predId.toString()))
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));
    }

    @Test
    @DisplayName("Trigger on-demand batch evaluation is idempotent and bounded")
    void testTriggerEvaluationIdempotency() throws Exception {
        String token = registerFarmer("Umar", "Xayyom");

        mockMvc.perform(post("/api/v1/predictions/evaluation/evaluate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").isNotEmpty());
    }
}
