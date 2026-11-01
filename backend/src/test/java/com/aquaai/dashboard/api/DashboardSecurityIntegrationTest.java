package com.aquaai.dashboard.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(30000);

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
        String email = "farmer." + id + "@dashboard.yangiariq.uz";

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
        String email = "operator." + id + "@dashboard.yangiariq.uz";

        User user = userRepository.save(User.builder()
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

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private String createAdminToken() {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99899" + String.format("%07d", id);
        String email = "admin." + id + "@dashboard.yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("Aqua")
                .fullName("Admin Aqua")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashed")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private UUID createField(String token, String name, BigDecimal area) throws Exception {
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
                "Dashboard parcel",
                area,
                CropType.COTTON,
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
    @DisplayName("Unauthenticated request to dashboard overview returns 401 Unauthorized")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer sees strictly own fields and metrics in overview")
    void farmerSeesOnlyOwnFields() throws Exception {
        String farmer1Token = registerFarmer("Anvar", "Karimov");
        String farmer2Token = registerFarmer("Dilshod", "Aliev");

        UUID f1 = createField(farmer1Token, "Anvar Field 1", new BigDecimal("10.0"));
        UUID f2 = createField(farmer1Token, "Anvar Field 2", new BigDecimal("15.5"));
        UUID f3 = createField(farmer2Token, "Dilshod Field 1", new BigDecimal("20.0"));

        // Farmer 1 Overview
        MvcResult res1 = mockMvc.perform(get("/api/v1/dashboard/overview")
                        .header("Authorization", "Bearer " + farmer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldMetrics.totalFields").value(2))
                .andReturn();

        JsonNode data1 = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data");
        assertThat(data1.get("priorityFields")).hasSize(2);
        assertThat(data1.get("tenantScope").asText()).contains("FARMER");

        // Farmer 2 Overview
        MvcResult res2 = mockMvc.perform(get("/api/v1/dashboard/overview")
                        .header("Authorization", "Bearer " + farmer2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldMetrics.totalFields").value(1))
                .andReturn();

        JsonNode data2 = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data");
        assertThat(data2.get("priorityFields")).hasSize(1);
        assertThat(data2.get("priorityFields").get(0).get("fieldName").asText()).isEqualTo("Dilshod Field 1");
    }

    @Test
    @DisplayName("Farmer cannot query another farmer's field: Anti-IDOR returns 404 Not Found")
    void farmerCannotQueryAnotherFarmerField() throws Exception {
        String farmer1Token = registerFarmer("Rustam", "Usmonov");
        String farmer2Token = registerFarmer("Sardor", "Qodirov");

        UUID foreignFieldId = createField(farmer2Token, "Sardor Private Field", new BigDecimal("8.0"));

        // Farmer 1 attempts to query Farmer 2's field
        mockMvc.perform(get("/api/v1/dashboard/overview")
                        .header("Authorization", "Bearer " + farmer1Token)
                        .param("fieldId", foreignFieldId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Operator has district-wide visibility across all parcels")
    void operatorHasDistrictWideVisibility() throws Exception {
        String farmerToken = registerFarmer("Zafar", "Nazarov");
        createField(farmerToken, "Zafar Parcel 1", new BigDecimal("5.0"));
        createField(farmerToken, "Zafar Parcel 2", new BigDecimal("7.0"));

        String operatorToken = createOperatorToken();

        MvcResult result = mockMvc.perform(get("/api/v1/dashboard/overview")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertThat(data.get("fieldMetrics").get("totalFields").asInt()).isGreaterThanOrEqualTo(2);
        assertThat(data.get("tenantScope").asText()).contains("OPERATOR");
    }

    @Test
    @DisplayName("Admin has system-wide global visibility")
    void adminHasGlobalVisibility() throws Exception {
        String adminToken = createAdminToken();

        MvcResult result = mockMvc.perform(get("/api/v1/dashboard/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertThat(data.get("tenantScope").asText()).contains("ADMIN");
        assertThat(data.get("waterBudget")).isNotNull();
        assertThat(data.get("urgentActions")).isNotNull();
    }
}
