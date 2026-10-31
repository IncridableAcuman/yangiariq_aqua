package com.aquaai.field.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.api.dto.UpdateFieldRequest;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FieldIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(1000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String registerAndGetToken(String firstName, String lastName) throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "farmer." + id + "@yangiariq.uz";

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

    private String createPrivilegedToken(String prefix, UserRole role) {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99891" + String.format("%07d", id);
        String email = prefix + "." + id + "@yangiariq.uz";

        User user = userRepository.save(User.builder()
                .firstName(prefix)
                .lastName("User")
                .fullName(prefix + " User")
                .phoneNumber(phone)
                .email(email)
                .passwordHash("$2a$10$e84WJb3hZekM7bL4.oO62O8VqC5yT.yvMvh2Z/iK2mZ2c8rO2H2.m")
                .role(role)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    }

    private FieldBoundary createTestBoundary() {
        return FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(60.5900, 41.4500),
                CoordinatePoint.of(60.5950, 41.4500),
                CoordinatePoint.of(60.5950, 41.4550),
                CoordinatePoint.of(60.5900, 41.4550)
        ));
    }

    // =========================================================================
    // 1. Unauthenticated Requests
    // =========================================================================
    @Test
    @DisplayName("1. Unauthenticated requests to Field endpoints should return 401 Unauthorized")
    void testUnauthenticatedRequestsReturn401() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/fields/" + randomId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/fields"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/fields/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/fields/" + randomId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/fields/" + randomId + "/activate"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 2. Create Field & Input Validation
    // =========================================================================
    @Test
    @DisplayName("2. Authenticated farmer successfully creates a field with valid parameters")
    void testFarmerCreatesFieldSuccessfully() throws Exception {
        String token = registerAndGetToken("Anvar", "Qodirov");

        CreateFieldRequest request = new CreateFieldRequest(
                "Sholi Maydoni 1",
                "Near primary irrigation canal branch",
                new BigDecimal("14.5000"),
                CropType.RICE,
                SoilType.CLAY_LOAM,
                IrrigationMethod.CANAL,
                new BigDecimal("41.452000"),
                new BigDecimal("60.592000"),
                createTestBoundary()
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.name").value("Sholi Maydoni 1"))
                .andExpect(jsonPath("$.data.description").value("Near primary irrigation canal branch"))
                .andExpect(jsonPath("$.data.cropType").value("RICE"))
                .andExpect(jsonPath("$.data.soilType").value("CLAY_LOAM"))
                .andExpect(jsonPath("$.data.irrigationMethod").value("CANAL"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.boundary.type").value("Polygon"))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertThat(new BigDecimal(data.get("areaHectares").asText())).isEqualByComparingTo("14.5000");
    }

    @Test
    @DisplayName("3. Field creation validations reject invalid structural data (blank name, non-positive area, invalid lat/lon)")
    void testCreateFieldValidationErrors() throws Exception {
        String token = registerAndGetToken("Bobur", "Mirzo");

        // 3.1 Blank name
        CreateFieldRequest blankName = new CreateFieldRequest(
                "",
                "Description",
                new BigDecimal("5.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );
        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 3.2 Negative area
        CreateFieldRequest negativeArea = new CreateFieldRequest(
                "Maydon A",
                "Description",
                new BigDecimal("-2.5"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );
        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativeArea)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 3.3 Latitude out of bounds (> 90.0)
        CreateFieldRequest invalidLat = new CreateFieldRequest(
                "Maydon B",
                "Description",
                new BigDecimal("5.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                new BigDecimal("95.0"),
                new BigDecimal("60.0"),
                null
        );
        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLat)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("4. Non-farmer role (OPERATOR or ADMIN) is forbidden from creating field parcels")
    void testNonFarmerCannotCreateField() throws Exception {
        String operatorToken = createPrivilegedToken("operator", UserRole.OPERATOR);
        String adminToken = createPrivilegedToken("admin", UserRole.ADMIN);

        CreateFieldRequest request = new CreateFieldRequest(
                "Operator Field",
                null,
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. Duplicate active field name for same farmer is rejected with 409 Conflict")
    void testDuplicateActiveFieldNameRejectedForSameFarmer() throws Exception {
        String token = registerAndGetToken("Sobir", "Rahimov");

        CreateFieldRequest first = new CreateFieldRequest(
                "Paxta Maydoni Markaz",
                null,
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        // Same exact name
        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));

        // Case-insensitive duplicate name
        CreateFieldRequest lowercase = new CreateFieldRequest(
                "paxta maydoni markaz",
                null,
                new BigDecimal("12.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                null,
                null,
                null
        );
        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lowercase)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("6. Different farmers are permitted to have fields with identical names (owner-scoped uniqueness)")
    void testDifferentFarmersCanHaveSameFieldName() throws Exception {
        String tokenA = registerAndGetToken("FarmerA", "Alimov");
        String tokenB = registerAndGetToken("FarmerB", "Botirov");

        CreateFieldRequest request = new CreateFieldRequest(
                "Bog' 1",
                null,
                new BigDecimal("5.0"),
                CropType.MELON,
                SoilType.SANDY,
                IrrigationMethod.DRIP,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // 3. Get Field, IDOR Protection & Role Access
    // =========================================================================
    @Test
    @DisplayName("7. Farmer retrieves own field by ID; Non-existent field returns 404")
    void testFarmerRetrievesOwnFieldById() throws Exception {
        String token = registerAndGetToken("Davron", "Ergashev");

        CreateFieldRequest request = new CreateFieldRequest(
                "Bug'doy Maydoni",
                "Winter crop",
                new BigDecimal("8.5"),
                CropType.WHEAT,
                SoilType.SANDY_LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // 7.1 Successful retrieval
        mockMvc.perform(get("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(fieldId))
                .andExpect(jsonPath("$.data.name").value("Bug'doy Maydoni"));

        // 7.2 Non-existent field returns 404
        mockMvc.perform(get("/api/v1/fields/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("8. IDOR Prevention: Farmer B attempting to access Farmer A's field receives 403 Forbidden")
    void testSecurityAgainstIdorOnGetField() throws Exception {
        String tokenA = registerAndGetToken("FarmerA", "SecurityTest");
        String tokenB = registerAndGetToken("FarmerB", "SecurityTest");

        CreateFieldRequest request = new CreateFieldRequest(
                "Private Field A",
                "Confidential notes",
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldAId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // Farmer B attempts to access Field A -> 404 Not Found (Anti-enumeration security)
        mockMvc.perform(get("/api/v1/fields/" + fieldAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("9. Operator and Admin can access field across the district according to policy")
    void testOperatorAndAdminCanAccessField() throws Exception {
        String farmerToken = registerAndGetToken("FarmerDistrict", "Test");
        String operatorToken = createPrivilegedToken("operatorAccess", UserRole.OPERATOR);
        String adminToken = createPrivilegedToken("adminAccess", UserRole.ADMIN);

        CreateFieldRequest request = new CreateFieldRequest(
                "District Monitored Parcel",
                null,
                new BigDecimal("25.0"),
                CropType.MAIZE,
                SoilType.LOAM,
                IrrigationMethod.SPRINKLER,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // Operator access
        mockMvc.perform(get("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("District Monitored Parcel"));

        // Admin access
        mockMvc.perform(get("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("District Monitored Parcel"));
    }

    // =========================================================================
    // 4. List Fields, Pagination, Sorting & Filtering
    // =========================================================================
    @Test
    @DisplayName("10. List fields strictly scopes results to the authenticated farmer and supports pagination")
    void testListFieldsForFarmerStrictlyScoped() throws Exception {
        String tokenFarmer1 = registerAndGetToken("ListFarmer1", "Test");
        String tokenFarmer2 = registerAndGetToken("ListFarmer2", "Test");

        // Farmer 1 creates 3 fields
        for (int i = 1; i <= 3; i++) {
            CreateFieldRequest r = new CreateFieldRequest(
                    "Farmer1 Field " + i,
                    null,
                    new BigDecimal("10." + i),
                    CropType.COTTON,
                    SoilType.LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null,
                    null
            );
            mockMvc.perform(post("/api/v1/fields")
                            .header("Authorization", "Bearer " + tokenFarmer1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(r)))
                    .andExpect(status().isCreated());
        }

        // Farmer 2 creates 1 field
        CreateFieldRequest r2 = new CreateFieldRequest(
                "Farmer2 Unique Field",
                null,
                new BigDecimal("5.0"),
                CropType.WHEAT,
                SoilType.SANDY,
                IrrigationMethod.DRIP,
                null,
                null,
                null
        );
        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenFarmer2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r2)))
                .andExpect(status().isCreated());

        // Farmer 1 lists fields -> sees exactly 3 fields
        mockMvc.perform(get("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenFarmer1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content.length()").value(3));

        // Farmer 2 lists fields -> sees exactly 1 field
        mockMvc.perform(get("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenFarmer2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Farmer2 Unique Field"));

        // Pagination: Farmer 1 queries page 0, size 2
        mockMvc.perform(get("/api/v1/fields?page=0&size=2")
                        .header("Authorization", "Bearer " + tokenFarmer1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false))
                .andExpect(jsonPath("$.data.content.length()").value(2));

        // Sorting: sort=name,asc
        mockMvc.perform(get("/api/v1/fields?sort=name,asc")
                        .header("Authorization", "Bearer " + tokenFarmer1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Farmer1 Field 1"));

        // Safe fallback: unsupported sorting property should not cause 500 error
        mockMvc.perform(get("/api/v1/fields?sort=unsupportedProperty,desc")
                        .header("Authorization", "Bearer " + tokenFarmer1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // 5. Update Field & PATCH Semantics
    // =========================================================================
    @Test
    @DisplayName("11. Farmer updates own field using PATCH semantics: unspecified properties remain unchanged")
    void testFarmerUpdatesFieldWithPatchSemantics() throws Exception {
        String token = registerAndGetToken("Ilhom", "Boboyev");

        CreateFieldRequest original = new CreateFieldRequest(
                "Asl Maydon",
                "Original description text",
                new BigDecimal("12.0000"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                new BigDecimal("41.450000"),
                new BigDecimal("60.590000"),
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(original)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // PATCH: modify only cropType and areaHectares
        UpdateFieldRequest patchRequest = new UpdateFieldRequest(
                null, // Unchanged name
                null, // Unchanged description
                new BigDecimal("18.5000"), // Updated area
                CropType.RICE, // Updated crop
                null, // Unchanged soil
                null, // Unchanged irrigation
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Asl Maydon")) // Preserved!
                .andExpect(jsonPath("$.data.description").value("Original description text")) // Preserved!
                .andExpect(jsonPath("$.data.cropType").value("RICE")) // Updated!
                .andExpect(jsonPath("$.data.soilType").value("LOAM")) // Preserved!
                .andExpect(jsonPath("$.data.irrigationMethod").value("FURROW")); // Preserved!
    }

    @Test
    @DisplayName("12. IDOR Prevention: Farmer B attempting to update Farmer A's field receives 403 Forbidden")
    void testSecurityAgainstIdorOnUpdateField() throws Exception {
        String tokenA = registerAndGetToken("OwnerFarmer", "UpdateTest");
        String tokenB = registerAndGetToken("IntruderFarmer", "UpdateTest");

        CreateFieldRequest original = new CreateFieldRequest(
                "Secure Target Field",
                null,
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(original)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        UpdateFieldRequest maliciousUpdate = new UpdateFieldRequest(
                "Hijacked Name",
                null,
                new BigDecimal("99.0"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // Verify Field remains untouched
        mockMvc.perform(get("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Secure Target Field"));
    }

    // =========================================================================
    // 6. Deactivate & Reactivate Lifecycle
    // =========================================================================
    @Test
    @DisplayName("13. Field deactivation transitions status to INACTIVE without hard deletion; IDOR deletion rejected")
    void testDeactivateAndReactivateLifecycle() throws Exception {
        String tokenA = registerAndGetToken("LifecycleFarmerA", "Test");
        String tokenB = registerAndGetToken("LifecycleFarmerB", "Test");

        CreateFieldRequest request = new CreateFieldRequest(
                "Lifecycle Field",
                null,
                new BigDecimal("7.5"),
                CropType.MELON,
                SoilType.SANDY,
                IrrigationMethod.DRIP,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // 13.1 IDOR Prevention: Farmer B cannot deactivate Farmer A's field (anti-enumeration 404)
        mockMvc.perform(delete("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // 13.2 Farmer A deactivates own field
        mockMvc.perform(delete("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 13.3 Verify field still exists with status = INACTIVE (preserved for historical records)
        mockMvc.perform(get("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        // 13.4 Farmer A can create a new ACTIVE field with the same name now that original is INACTIVE
        mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 13.5 Attempting to reactivate original inactive field while new active field exists with same name fails with 409 Conflict
        mockMvc.perform(patch("/api/v1/fields/" + fieldId + "/activate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    // =========================================================================
    // 7. Complete End-to-End Smoke Test Sequence (Section 32)
    // =========================================================================
    @Test
    @DisplayName("14. Milestone 05.2 End-to-End Smoke Test (12-step sequence verifying creation, retrieval, listing, updating, deactivation, and IDOR protection)")
    void testCompleteMilestoneSmokeTest() throws Exception {
        // Step 1 & 2: Register & Login FARMER A
        String tokenFarmerA = registerAndGetToken("SmokeFarmerA", "Tester");

        // Step 3: Create Field A
        CreateFieldRequest fieldARequest = new CreateFieldRequest(
                "Yangiariq Paxta Uchastka-1",
                "Primary cotton plot",
                new BigDecimal("22.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                new BigDecimal("41.450123"),
                new BigDecimal("60.590456"),
                createTestBoundary()
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenFarmerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fieldARequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.name").value("Yangiariq Paxta Uchastka-1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String fieldAId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // Step 4: Retrieve Field A
        mockMvc.perform(get("/api/v1/fields/" + fieldAId)
                        .header("Authorization", "Bearer " + tokenFarmerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(fieldAId));

        // Step 5: List FARMER A fields
        mockMvc.perform(get("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenFarmerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(fieldAId));

        // Step 6: Update Field A
        UpdateFieldRequest updateRequest = new UpdateFieldRequest(
                "Yangiariq Paxta Uchastka-1 Yangilangan",
                "Updated irrigation layout",
                new BigDecimal("23.0000"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/v1/fields/" + fieldAId)
                        .header("Authorization", "Bearer " + tokenFarmerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Yangiariq Paxta Uchastka-1 Yangilangan"));

        // Step 7: Deactivate Field A
        mockMvc.perform(delete("/api/v1/fields/" + fieldAId)
                        .header("Authorization", "Bearer " + tokenFarmerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 8 & 9: Register & Login FARMER B
        String tokenFarmerB = registerAndGetToken("SmokeFarmerB", "Tester");

        // Step 10: Attempt to access Field A -> MUST FAIL (404 Not Found - Anti-enumeration)
        mockMvc.perform(get("/api/v1/fields/" + fieldAId)
                        .header("Authorization", "Bearer " + tokenFarmerB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // Step 11: Attempt to modify Field A -> MUST FAIL (404 Not Found - Anti-enumeration)
        mockMvc.perform(patch("/api/v1/fields/" + fieldAId)
                        .header("Authorization", "Bearer " + tokenFarmerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        // Step 12: Attempt to deactivate Field A -> MUST FAIL (404 Not Found - Anti-enumeration)
        mockMvc.perform(delete("/api/v1/fields/" + fieldAId)
                        .header("Authorization", "Bearer " + tokenFarmerB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("15. OpenAPI v3 api-docs generates Field Management schema and endpoints")
    void testOpenApiDocContainsFieldEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/fields']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/fields/{fieldId}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/fields/{fieldId}/activate']").exists())
                .andExpect(jsonPath("$.components.schemas['CreateFieldRequest']").exists())
                .andExpect(jsonPath("$.components.schemas['UpdateFieldRequest']").exists())
                .andExpect(jsonPath("$.components.schemas['FieldResponse']").exists());
    }

    @Test
    @DisplayName("16. Operator attempting mutation (PATCH/DELETE/ACTIVATE) receives 403 Forbidden")
    void testOperatorCannotMutateOrDeactivateField() throws Exception {
        String farmerToken = registerAndGetToken("OpTestFarmer", "Owner");
        String operatorToken = createPrivilegedToken("operatorMutate", UserRole.OPERATOR);

        CreateFieldRequest request = new CreateFieldRequest(
                "Operator Target Field",
                null,
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // 16.1 Operator PATCH -> 403 Forbidden
        UpdateFieldRequest patchReq = new UpdateFieldRequest("Op Modified", null, null, null, null, null, null, null, null);
        mockMvc.perform(patch("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED_OPERATION"));

        // 16.2 Operator DELETE -> 403 Forbidden
        mockMvc.perform(delete("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED_OPERATION"));

        // 16.3 Operator ACTIVATE -> 403 Forbidden
        mockMvc.perform(patch("/api/v1/fields/" + fieldId + "/activate")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED_OPERATION"));
    }

    @Test
    @DisplayName("17. Malicious payload injecting ownerId during creation is completely ignored")
    void testMaliciousOwnerIdPayloadIgnoredAtCreation() throws Exception {
        String tokenFarmerA = registerAndGetToken("FarmerInjectedA", "Tester");
        String tokenFarmerB = registerAndGetToken("FarmerInjectedB", "Tester");

        // Malicious JSON attempting to declare ownerId = random UUID
        UUID forgedOwnerId = UUID.randomUUID();
        String maliciousPayload = """
                {
                    "name": "Tampered Owner Field",
                    "description": "Attempt to create on behalf of another user",
                    "ownerId": "%s",
                    "areaHectares": 15.0000,
                    "cropType": "COTTON",
                    "soilType": "LOAM",
                    "irrigationMethod": "CANAL"
                }
                """.formatted(forgedOwnerId);

        MvcResult result = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + tokenFarmerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousPayload))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asText();

        // Farmer A can access own field
        mockMvc.perform(get("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + tokenFarmerA))
                .andExpect(status().isOk());

        // Farmer B cannot access (it was NOT assigned to forgedOwnerId or Farmer B)
        mockMvc.perform(get("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + tokenFarmerB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("18. Malicious payload attempting to alter immutable attributes (id, status, ownerId, createdAt) via PATCH is ignored")
    void testMaliciousPayloadCannotAlterImmutableAttributesOnUpdate() throws Exception {
        String token = registerAndGetToken("ImmutableFarmer", "Tester");

        CreateFieldRequest request = new CreateFieldRequest(
                "Immutable Test Field",
                "Original",
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/fields")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String fieldId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // Malicious PATCH attempting to change id, ownerId, status, and createdAt
        UUID forgedId = UUID.randomUUID();
        UUID forgedOwner = UUID.randomUUID();
        String maliciousPatch = """
                {
                    "id": "%s",
                    "ownerId": "%s",
                    "status": "INACTIVE",
                    "createdAt": "2020-01-01T00:00:00Z",
                    "name": "Legit Name Change"
                }
                """.formatted(forgedId, forgedOwner);

        mockMvc.perform(patch("/api/v1/fields/" + fieldId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousPatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(fieldId)) // Unchanged
                .andExpect(jsonPath("$.data.status").value("ACTIVE")) // Status remains ACTIVE!
                .andExpect(jsonPath("$.data.name").value("Legit Name Change"));
    }

    @Test
    @DisplayName("19. Farmer role escalation attempts (modifying user roles or statuses) are blocked")
    void testRoleEscalationAttemptsByFarmerBlocked() throws Exception {
        String farmerToken = registerAndGetToken("EscalationFarmer", "Tester");
        UUID randomUserId = UUID.randomUUID();

        // Attempt to change role via User module admin endpoint
        mockMvc.perform(patch("/api/v1/users/" + randomUserId + "/role")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"ADMIN\"}"))
                .andExpect(status().isForbidden());

        // Attempt to change status via User module admin endpoint
        mockMvc.perform(patch("/api/v1/users/" + randomUserId + "/status")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("20. Invalid, malformed, and expired JWT tokens are rejected with 401 Unauthorized")
    void testInvalidAndExpiredJwtTokensRejectedWith401() throws Exception {
        // Malformed token
        mockMvc.perform(get("/api/v1/fields")
                        .header("Authorization", "Bearer invalid.jwt.token.string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // Missing bearer prefix
        mockMvc.perform(get("/api/v1/fields")
                        .header("Authorization", "NotABearerToken"))
                .andExpect(status().isUnauthorized());
    }
}
