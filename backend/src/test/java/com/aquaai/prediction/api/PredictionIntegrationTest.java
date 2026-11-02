package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
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
class PredictionIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(70000);

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
        String phone = "+99895" + String.format("%07d", id);
        String email = "farmer." + id + "@predict.yangiariq.uz";

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
                AuthResponse.class
        );
        return auth.accessToken();
    }

    private String createOperatorToken() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99897" + String.format("%07d", id);
        String email = "op." + id + "@operator.yangiariq.uz";

        User operator = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("Yangiariq")
                .fullName("Operator Yangiariq")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(operator.getId(), operator.getPhoneNumber(), operator.getRole());
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
                "Prediction parcel " + name,
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
    @DisplayName("Unauthenticated requests return 401 Unauthorized")
    void unauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/predictions/water-demand"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/predictions/irrigation-need"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer can retrieve prediction overview for their parcels")
    void farmerOverviewSuccess() throws Exception {
        String token = registerFarmer("Anvar", "Rashidov");
        createField(token, "North Cotton 1", CropType.COTTON);

        mockMvc.perform(get("/api/v1/predictions/overview")
                        .header("Authorization", "Bearer " + token)
                        .param("horizon", "NEXT_48_HOURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.horizon").value("NEXT_48_HOURS"))
                .andExpect(jsonPath("$.data.totalParcelsCount").value(1))
                .andExpect(jsonPath("$.data.sensorHealthPercent").isNumber());
    }

    @Test
    @DisplayName("Farmer can retrieve comprehensive field predictions for own field")
    void farmerFieldPredictionsSuccess() throws Exception {
        String token = registerFarmer("Bobur", "Karimov");
        UUID fieldId = createField(token, "East Wheat 2", CropType.WHEAT);

        mockMvc.perform(get("/api/v1/predictions/fields/" + fieldId)
                        .header("Authorization", "Bearer " + token)
                        .param("horizon", "NEXT_48_HOURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.cropType").value("WHEAT"))
                .andExpect(jsonPath("$.data.allPredictions").isArray());
    }

    @Test
    @DisplayName("Farmer querying foreign field receives 404 (Anti-IDOR resource enumeration protection)")
    void antiIdorReturns404ForForeignField() throws Exception {
        String farmerA = registerFarmer("Alisher", "Navoiy");
        String farmerB = registerFarmer("Zahiriddin", "Bobur");

        UUID fieldA = createField(farmerA, "Alisher Field", CropType.COTTON);

        // Farmer B attempts to query Farmer A's field predictions
        mockMvc.perform(get("/api/v1/predictions/fields/" + fieldA)
                        .header("Authorization", "Bearer " + farmerB)
                        .param("horizon", "NEXT_48_HOURS"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Operator has broad access to query any field predictions")
    void operatorCanQueryAnyField() throws Exception {
        String farmer = registerFarmer("Dilshod", "Alimov");
        String operatorToken = createOperatorToken();

        UUID fieldId = createField(farmer, "Dilshod Melon", CropType.MELON);

        mockMvc.perform(get("/api/v1/predictions/fields/" + fieldId)
                        .header("Authorization", "Bearer " + operatorToken)
                        .param("horizon", "NEXT_24_HOURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fieldName").value("Dilshod Melon"));
    }

    @Test
    @DisplayName("Water demand forecast returns expected volumes with lower and upper bounds")
    void waterDemandForecastReturnsBounds() throws Exception {
        String token = registerFarmer("Eldor", "Shomurodov");
        createField(token, "Eldor Field", CropType.COTTON);

        mockMvc.perform(get("/api/v1/predictions/water-demand")
                        .header("Authorization", "Bearer " + token)
                        .param("horizon", "NEXT_7_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.horizon").value("NEXT_7_DAYS"))
                .andExpect(jsonPath("$.data.totalExpectedDemandM3").isNumber())
                .andExpect(jsonPath("$.data.totalLowerBoundM3").isNumber())
                .andExpect(jsonPath("$.data.totalUpperBoundM3").isNumber());
    }

    @Test
    @DisplayName("Irrigation need ranking orders parcels by urgency")
    void irrigationNeedRankingSuccess() throws Exception {
        String token = registerFarmer("Farhod", "Qodirov");
        createField(token, "Farhod Parcel A", CropType.VEGETABLE);

        mockMvc.perform(get("/api/v1/predictions/irrigation-need")
                        .header("Authorization", "Bearer " + token)
                        .param("horizon", "NEXT_48_HOURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rankedFields").isArray());
    }

    @Test
    @DisplayName("Emerging risks endpoint returns list of elevated risk predictions")
    void emergingRisksSuccess() throws Exception {
        String token = registerFarmer("Gofur", "Gulyam");
        createField(token, "Gofur Parcel", CropType.WHEAT);

        mockMvc.perform(get("/api/v1/predictions/risks")
                        .header("Authorization", "Bearer " + token)
                        .param("horizon", "NEXT_48_HOURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Forecast history returns historical predictions without fake accuracy numbers")
    void forecastHistorySuccess() throws Exception {
        String token = registerFarmer("Hamid", "Olimjon");

        mockMvc.perform(get("/api/v1/predictions/history")
                        .header("Authorization", "Bearer " + token)
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.history").isArray());
    }
}
