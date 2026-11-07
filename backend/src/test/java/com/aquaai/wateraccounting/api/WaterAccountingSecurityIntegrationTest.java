package com.aquaai.wateraccounting.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.aquaai.wateraccounting.api.dto.ReconciliationReviewRequest;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.ReconciliationStatus;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WaterAccountingSecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 8200000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private WaterReconciliationRepository reconciliationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private record RegisteredFarmer(String token, UUID userId) {}

    private RegisteredFarmer registerFarmer() throws Exception {
        long id = USER_COUNTER.incrementAndGet();
        String phone = "+99890" + String.format("%07d", id);
        String email = "accounting.farmer." + id + "." + UUID.randomUUID().toString().substring(0, 5) + "@yangiariq.uz";

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Accounting")
                .lastName("Farmer")
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
        return new RegisteredFarmer(auth.accessToken(), auth.user().id());
    }

    private String createOperatorToken() {
        long id = USER_COUNTER.incrementAndGet();
        User op = userRepository.save(User.builder()
                .firstName("Accounting")
                .lastName("Operator")
                .fullName("Accounting District Operator")
                .phoneNumber("+99893" + String.format("%07d", id))
                .email("accounting.operator." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForOperator123456789")
                .role(UserRole.OPERATOR)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        return jwtTokenProvider.generateAccessToken(op.getId(), op.getEmail(), UserRole.OPERATOR);
    }

    @Test
    @DisplayName("Unauthenticated request to water accounting overview fails with 401")
    void unauthenticatedRequestFails() throws Exception {
        mockMvc.perform(get("/api/v1/water-accounting/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated farmer can view overview, balance, and losses")
    void authenticatedUserCanViewOverviewAndBalance() throws Exception {
        RegisteredFarmer farmer = registerFarmer();

        mockMvc.perform(get("/api/v1/water-accounting/overview")
                        .header("Authorization", "Bearer " + farmer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtBalance").isMap())
                .andExpect(jsonPath("$.data.totalLedgerEntries").isNumber());

        mockMvc.perform(get("/api/v1/water-accounting/balance")
                        .header("Authorization", "Bearer " + farmer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boundaryDimension").value("DISTRICT"));

        mockMvc.perform(get("/api/v1/water-accounting/conveyance-losses")
                        .header("Authorization", "Bearer " + farmer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Farmer cannot review reconciliation discrepancies (403 Forbidden)")
    void farmerCannotReviewReconciliation() throws Exception {
        RegisteredFarmer farmer = registerFarmer();
        UUID fakeId = UUID.randomUUID();

        ReconciliationReviewRequest req = new ReconciliationReviewRequest("Unauthorized review attempt");

        mockMvc.perform(post("/api/v1/water-accounting/reconciliation/" + fakeId + "/review")
                        .header("Authorization", "Bearer " + farmer.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operator can review reconciliation discrepancy")
    void operatorCanReviewReconciliation() throws Exception {
        String opToken = createOperatorToken();

        WaterReconciliationItem item = reconciliationRepository.save(WaterReconciliationItem.builder()
                .id(UUID.randomUUID())
                .boundaryType("SECONDARY_TO_FIELD")
                .boundaryName("Polvon Canal to Lateral Flume")
                .periodStart(Instant.now().minusSeconds(86400 * 7))
                .periodEnd(Instant.now())
                .expectedVolumeM3(new BigDecimal("15000.00"))
                .recordedVolumeM3(new BigDecimal("12500.00"))
                .discrepancyVolumeM3(new BigDecimal("-2500.00"))
                .discrepancyPercent(new BigDecimal("16.67"))
                .tolerancePercent(new BigDecimal("5.00"))
                .status(ReconciliationStatus.DISCREPANCY)
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .explanation("Unaccounted transmission drop")
                .build());

        ReconciliationReviewRequest req = new ReconciliationReviewRequest("Reviewed and verified acoustic gauge variance");

        mockMvc.perform(post("/api/v1/water-accounting/reconciliation/" + item.getId() + "/review")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("WITHIN_TOLERANCE"))
                .andExpect(jsonPath("$.data.reviewNotes").value("Reviewed and verified acoustic gauge variance"));
    }

    @Test
    @DisplayName("Anti-IDOR: Farmer cannot view another farmer's parcel water accounting (403 Forbidden)")
    void farmerCannotViewAnotherFarmersParcelAccounting() throws Exception {
        RegisteredFarmer farmerA = registerFarmer();
        RegisteredFarmer farmerB = registerFarmer();

        // Field belonging to Farmer B
        Field fieldB = fieldRepository.save(Field.create(
                farmerB.userId(),
                "Farmer B Cotton Field",
                "Test field for accounting IDOR check",
                new BigDecimal("8.50"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                new FieldLocation(new BigDecimal("41.365000"), new BigDecimal("60.590000")),
                null
        ));

        // Farmer A attempts to access Farmer B's field accounting
        mockMvc.perform(get("/api/v1/water-accounting/fields/" + fieldB.getId())
                        .header("Authorization", "Bearer " + farmerA.token()))
                .andExpect(status().isForbidden());

        // Farmer B accesses their own field accounting successfully
        mockMvc.perform(get("/api/v1/water-accounting/fields/" + fieldB.getId())
                        .header("Authorization", "Bearer " + farmerB.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fieldId").value(fieldB.getId().toString()))
                .andExpect(jsonPath("$.data.fieldName").value("Farmer B Cotton Field"));
    }
}
