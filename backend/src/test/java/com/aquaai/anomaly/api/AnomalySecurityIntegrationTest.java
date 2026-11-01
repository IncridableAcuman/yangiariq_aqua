package com.aquaai.anomaly.api;

import com.aquaai.anomaly.api.dto.ActionAnomalyRequest;
import com.aquaai.anomaly.api.dto.UpdateAnomalyRuleRequest;
import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.anomaly.domain.repository.AnomalyRuleConfigRepository;
import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.sensor.domain.model.SensorType;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnomalySecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(9800);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnomalyRepository anomalyRepository;

    @Autowired
    private AnomalyRuleConfigRepository ruleConfigRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String registerFarmer(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "farmer." + id + "@anomaly.yangiariq.uz";

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
        String email = "operator." + id + "@anomaly.yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("District")
                .fullName("Operator District")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private String createAdminToken() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99899" + String.format("%07d", id);
        String email = "admin." + id + "@anomaly.yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName("Super")
                .lastName("Admin")
                .fullName("Super Admin")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private String createField(String token, String name) throws Exception {
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(60.60, 41.33),
                CoordinatePoint.of(60.61, 41.33),
                CoordinatePoint.of(60.61, 41.34),
                CoordinatePoint.of(60.60, 41.34),
                CoordinatePoint.of(60.60, 41.33)
        ));

        CreateFieldRequest request = new CreateFieldRequest(
                name,
                "Parcel description",
                new BigDecimal("12.50"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                new BigDecimal("41.340000"),
                new BigDecimal("60.620000"),
                boundary
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("data").get("id").asText();
    }

    @Test
    @DisplayName("Anti-IDOR: Farmer can view own parcel anomaly, but receives 404 for another farmer's anomaly")
    void farmerAntiIdorEnforcement() throws Exception {
        String tokenFarmer1 = registerFarmer("Anvar", "Qodirov");
        String tokenFarmer2 = registerFarmer("Rustam", "Aliyev");

        String field1Id = createField(tokenFarmer1, "Anvar Paxtazor");

        // Seed anomaly on field 1
        AnomalyCandidate candidate = new AnomalyCandidate(
                AnomalyType.SOIL_MOISTURE_CRITICAL,
                AnomalySeverity.CRITICAL,
                BigDecimal.valueOf(11.2),
                BigDecimal.valueOf(15.0),
                BigDecimal.valueOf(-3.8),
                BigDecimal.valueOf(0.92),
                "Critical soil moisture deficit",
                "Moisture dropped to 11.2%",
                "RULE_ENGINE",
                List.of(),
                false
        );
        Anomaly anomaly = anomalyRepository.save(Anomaly.create(
                UUID.fromString(field1Id), null, SensorType.SOIL_MOISTURE, candidate, Instant.now()
        ));

        // 1. Farmer 1 can view own anomaly -> 200 OK
        mockMvc.perform(get("/api/v1/anomalies/{id}", anomaly.getId())
                        .header("Authorization", "Bearer " + tokenFarmer1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(anomaly.getId().toString()))
                .andExpect(jsonPath("$.data.anomalyType").value("SOIL_MOISTURE_CRITICAL"));

        // 2. Farmer 2 accessing Farmer 1's anomaly -> 404 Not Found (Anti-enumeration!)
        mockMvc.perform(get("/api/v1/anomalies/{id}", anomaly.getId())
                        .header("Authorization", "Bearer " + tokenFarmer2))
                .andExpect(status().isNotFound());

        // 3. Farmer 2 attempting to acknowledge Farmer 1's anomaly -> 404 Not Found
        mockMvc.perform(patch("/api/v1/anomalies/{id}/acknowledge", anomaly.getId())
                        .header("Authorization", "Bearer " + tokenFarmer2))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Operator has district-wide visibility and can acknowledge operational anomalies")
    void operatorDistrictWideAccess() throws Exception {
        String tokenFarmer = registerFarmer("Dilshod", "Boboyev");
        String tokenOperator = createOperatorToken();

        String fieldId = createField(tokenFarmer, "Dilshod Bog'");

        AnomalyCandidate candidate = new AnomalyCandidate(
                AnomalyType.WATER_FLOW_SPIKE,
                AnomalySeverity.HIGH,
                BigDecimal.valueOf(52.0),
                BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(27.0),
                BigDecimal.valueOf(0.85),
                "Flow spike breach",
                "Flow exceeded nominal rate",
                "RULE_ENGINE",
                List.of(),
                false
        );
        Anomaly anomaly = anomalyRepository.save(Anomaly.create(
                UUID.fromString(fieldId), null, SensorType.WATER_FLOW, candidate, Instant.now()
        ));

        // Operator can view anomaly -> 200 OK
        mockMvc.perform(get("/api/v1/anomalies/{id}", anomaly.getId())
                        .header("Authorization", "Bearer " + tokenOperator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(anomaly.getId().toString()));

        // Operator can acknowledge anomaly -> 200 OK
        mockMvc.perform(patch("/api/v1/anomalies/{id}/acknowledge", anomaly.getId())
                        .header("Authorization", "Bearer " + tokenOperator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACKNOWLEDGED"));
    }

    @Test
    @DisplayName("Admin can update global anomaly rules; non-admin is rejected with 403 Forbidden")
    void ruleManagementSecurity() throws Exception {
        String tokenFarmer = registerFarmer("Sobir", "Karimov");
        String tokenAdmin = createAdminToken();

        var rules = ruleConfigRepository.findAll();
        var rule = rules.get(0);

        UpdateAnomalyRuleRequest request = new UpdateAnomalyRuleRequest(
                "Updated Rule Title",
                "Adjusted agronomic threshold for testing",
                AnomalySeverity.CRITICAL,
                new BigDecimal("18.5"),
                new BigDecimal("28.0"),
                45,
                2
        );

        // 1. Farmer attempting to update rule -> 403 Forbidden
        mockMvc.perform(put("/api/v1/anomaly-rules/{id}", rule.getId())
                        .header("Authorization", "Bearer " + tokenFarmer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 2. Admin updating rule -> 200 OK
        mockMvc.perform(put("/api/v1/anomaly-rules/{id}", rule.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Rule Title"))
                .andExpect(jsonPath("$.data.threshold").value(18.5));
    }
}
