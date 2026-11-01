package com.aquaai.intelligence.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
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
class IntelligenceSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 7000000);

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
        String email = "intel.farmer." + id + "." + UUID.randomUUID().toString().substring(0, 5) + "@yangiariq.uz";

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
                .firstName("Intel")
                .lastName("Operator")
                .fullName("Intel District Operator")
                .phoneNumber("+99893" + String.format("%07d", id))
                .email("intel.operator." + id + "@yangiariq.uz")
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
                .firstName("Intel")
                .lastName("Admin")
                .fullName("Intel Admin")
                .phoneNumber("+99894" + String.format("%07d", id))
                .email("intel.admin." + id + "@yangiariq.uz")
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
                "Automated test plot in Yangiariq for intelligence evaluation",
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

        String idStr = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asText();
        return UUID.fromString(idStr);
    }

    @Test
    @DisplayName("Unauthenticated request to intelligence endpoint returns 401 Unauthorized")
    void unauthenticatedReturns401() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/fields/" + randomId + "/intelligence/assessment"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/fields/" + randomId + "/intelligence/evaluate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer cannot access or evaluate foreign field intelligence - Anti-IDOR returns 404")
    void farmerForeignFieldReturns404() throws Exception {
        String farmerAToken = registerFarmer("Anvar", "Karimov");
        String farmerBToken = registerFarmer("Bobur", "Alimov");

        UUID fieldA = createFieldForFarmer(farmerAToken, "Anvar's Field");
        UUID fieldB = createFieldForFarmer(farmerBToken, "Bobur's Field");

        // Farmer A accessing Farmer B's field returns 404 Not Found (anti-enumeration)
        mockMvc.perform(get("/api/v1/fields/" + fieldB + "/intelligence/assessment")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/fields/" + fieldB + "/intelligence/evaluate")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/fields/" + fieldB + "/intelligence/signals")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/fields/" + fieldB + "/intelligence/history")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/fields/" + fieldB + "/recommendations/evaluate")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Farmer can evaluate and access own field intelligence")
    void farmerOwnFieldEvaluationSucceeds() throws Exception {
        String token = registerFarmer("Jasur", "Nazarov");
        UUID fieldId = createFieldForFarmer(token, "Jasur's Melon Parcel");

        // 1. Evaluate intelligence on own field
        mockMvc.perform(post("/api/v1/fields/" + fieldId + "/intelligence/evaluate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assessment.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.recommendation.fieldId").value(fieldId.toString()));

        // 2. Query assessment
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/intelligence/assessment")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.data.primarySignal").exists())
                .andExpect(jsonPath("$.data.confidence").exists());

        // 3. Query signals
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/intelligence/signals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 4. Query history
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/intelligence/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // 5. Test recommendation evaluate endpoint
        mockMvc.perform(post("/api/v1/fields/" + fieldId + "/recommendations/evaluate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fieldId").value(fieldId.toString()));
    }

    @Test
    @DisplayName("District Operator and Admin have authorized access to parcel intelligence")
    void operatorAndAdminCanAccessIntelligence() throws Exception {
        String farmerToken = registerFarmer("Sardor", "Bekov");
        UUID fieldId = createFieldForFarmer(farmerToken, "Sardor's Parcel");

        String opToken = createOperatorToken();
        String adminToken = createAdminToken();

        // Operator access
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/intelligence/assessment")
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk());

        // Admin access
        mockMvc.perform(get("/api/v1/fields/" + fieldId + "/intelligence/assessment")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
