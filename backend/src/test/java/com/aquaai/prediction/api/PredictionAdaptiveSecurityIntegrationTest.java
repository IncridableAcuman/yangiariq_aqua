package com.aquaai.prediction.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.prediction.application.dto.SelectAdaptiveMethodRequest;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
class PredictionAdaptiveSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(89000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String farmer1Token;
    private String farmer2Token;
    private String operatorToken;
    private UUID farmer1FieldId;

    @BeforeEach
    void setUp() throws Exception {
        farmer1Token = registerFarmer("FarmerOne");
        farmer2Token = registerFarmer("FarmerTwo");

        long opId = USER_COUNTER.incrementAndGet();
        User operator = userRepository.save(User.builder()
                .firstName("Operator")
                .lastName("Yangiariq")
                .fullName("Operator Yangiariq")
                .phoneNumber("+99893" + String.format("%07d", opId))
                .email("op." + opId + "@operator.yangiariq.uz")
                .passwordHash("hashed")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
        operatorToken = jwtTokenProvider.generateAccessToken(operator.getId(), operator.getPhoneNumber(), operator.getRole());

        // Farmer 1 creates a parcel
        CreateFieldRequest fieldReq = new CreateFieldRequest(
                "Farmer1 Parcel",
                "Description",
                BigDecimal.valueOf(10.0),
                CropType.WHEAT,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                BigDecimal.valueOf(41.35),
                BigDecimal.valueOf(60.60),
                FieldBoundary.fromExteriorRing(List.of(
                        CoordinatePoint.of(60.60, 41.35),
                        CoordinatePoint.of(60.61, 41.35),
                        CoordinatePoint.of(60.61, 41.36),
                        CoordinatePoint.of(60.60, 41.36),
                        CoordinatePoint.of(60.60, 41.35)
                ))
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + farmer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fieldReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        farmer1FieldId = UUID.fromString(root.get("data").get("id").asText());
    }

    private String registerFarmer(String name) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        RegisterRequest req = RegisterRequest.builder()
                .firstName(name)
                .lastName("Uz")
                .phoneNumber("+99890" + String.format("%07d", id))
                .email(name.toLowerCase() + "." + id + "@farm.uz")
                .password("SecurePass123!")
                .district("Yangiariq")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
        return auth.accessToken();
    }

    @Test
    @DisplayName("Unauthenticated requests return 401 Unauthorized")
    void unauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/adaptive/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/predictions/adaptive/context/" + farmer1FieldId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer 2 accessing Farmer 1 parcel context returns 404 (Anti-IDOR)")
    void farmerCannotAccessOtherFarmerParcelContext() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/adaptive/context/" + farmer1FieldId)
                        .header("Authorization", "Bearer " + farmer2Token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Farmer 2 triggering adaptive select on Farmer 1 parcel returns 404 (Anti-IDOR)")
    void farmerCannotSelectOnOtherFarmerParcel() throws Exception {
        SelectAdaptiveMethodRequest req = new SelectAdaptiveMethodRequest(farmer1FieldId, "SOIL_MOISTURE_FORECAST");

        mockMvc.perform(post("/api/v1/predictions/adaptive/select")
                        .header("Authorization", "Bearer " + farmer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Operator can inspect parcel context for any district field")
    void operatorCanAccessAnyParcelContext() throws Exception {
        mockMvc.perform(get("/api/v1/predictions/adaptive/context/" + farmer1FieldId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fieldId").value(farmer1FieldId.toString()));
    }
}
