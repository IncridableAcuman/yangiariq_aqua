package com.aquaai.accountability.api;

import com.aquaai.accountability.api.dto.ApproveActionRequest;
import com.aquaai.accountability.api.dto.AssignInvestigatorRequest;
import com.aquaai.accountability.api.dto.CreateIncidentRequest;
import com.aquaai.accountability.api.dto.ProposeActionRequest;
import com.aquaai.accountability.api.dto.ReviewIncidentRequest;
import com.aquaai.accountability.api.dto.VerifyResolutionRequest;
import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.VerificationOutcome;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountabilitySecurityIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 9100000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User createTestUser(UserRole role) {
        long id = USER_COUNTER.incrementAndGet();
        return userRepository.save(User.builder()
                .firstName("Test")
                .lastName("Accountability")
                .fullName("Test Accountability " + id)
                .phoneNumber("+99890" + String.format("%07d", id))
                .email("accountability." + id + "@yangiariq.uz")
                .passwordHash("$2a$10$hashedPasswordTestingAccountabilityPlaceholder123456789")
                .role(role)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
    }

    private String createToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Test
    @DisplayName("Unauthenticated requests to accountability APIs return 401")
    void unauthenticatedRequestsShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/accountability/incidents"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/accountability/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Farmer cannot perform operator review or approval actions")
    void farmerCannotReviewOrApproveActions() throws Exception {
        User farmer = createTestUser(UserRole.FARMER);
        String farmerToken = createToken(farmer);
        UUID incidentId = UUID.randomUUID();

        ReviewIncidentRequest reviewReq = new ReviewIncidentRequest(true, "Farmer attempting review");
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/review")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isForbidden());

        ApproveActionRequest approveReq = new ApproveActionRequest(UUID.randomUUID(), "Farmer approving");
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/approve-action")
                        .header("Authorization", "Bearer " + farmerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Farmer cannot access incident of another farmer's field (Anti-IDOR)")
    void farmerCannotAccessOtherFarmerIncident() throws Exception {
        User farmer1 = createTestUser(UserRole.FARMER);
        String farmer1Token = createToken(farmer1);

        User farmer2 = createTestUser(UserRole.FARMER);

        Field field2 = fieldRepository.save(Field.create(
                farmer2.getId(),
                "Farmer 2 Cotton Field",
                "Parcel description",
                new BigDecimal("12.5"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                FieldLocation.of(41.34, 60.61),
                null
        ));

        WaterIncident inc = incidentRepository.save(WaterIncident.builder()
                .title("Field 2 Water Inflow Discrepancy")
                .description("Farmer 2 turnout overflow")
                .category(IncidentCategory.WATER_BALANCE_DISCREPANCY)
                .severity(IncidentSeverity.MEDIUM)
                .confidence(EvidenceConfidence.VERIFIED)
                .status(IncidentStatus.OPEN)
                .sourceModule("WATER_ACCOUNTING")
                .sourceEntityType("FIELD")
                .fieldId(field2.getId())
                .build());

        // Farmer 1 queries Field 2's incident -> must be blocked
        mockMvc.perform(get("/api/v1/accountability/incidents/" + inc.getId())
                        .header("Authorization", "Bearer " + farmer1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operator can view dashboard, list incidents, review, and propose actions")
    void operatorCanManageLifecycle() throws Exception {
        User operator = createTestUser(UserRole.OPERATOR);
        String operatorToken = createToken(operator);

        // 1. View Dashboard
        mockMvc.perform(get("/api/v1/accountability/dashboard")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Create Incident
        CreateIncidentRequest createReq = new CreateIncidentRequest(
                "Canal Bank Seepage at Station 3",
                "Visible wetting on eastern embankment",
                IncidentCategory.CONVEYANCE_LOSS,
                IncidentSeverity.HIGH,
                EvidenceConfidence.PARTIALLY_VERIFIED,
                "MANUAL",
                "INSPECTION",
                null,
                "INSP-ST3",
                null,
                null,
                null,
                "Canal Reach 3 PK15",
                Instant.now()
        );

        String createRes = mockMvc.perform(post("/api/v1/accountability/incidents")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Canal Bank Seepage at Station 3"))
                .andExpect(jsonPath("$.data.status").value("DETECTED"))
                .andReturn().getResponse().getContentAsString();

        UUID incidentId = UUID.fromString(objectMapper.readTree(createRes).path("data").path("id").asText());

        // 3. Review Incident -> OPEN
        ReviewIncidentRequest reviewReq = new ReviewIncidentRequest(true, "Confirmed valid operational issue");
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/review")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        // 4. Assign Investigator
        AssignInvestigatorRequest assignReq = new AssignInvestigatorRequest(
                operator.getId(),
                "Operator Incharge",
                "Yangiariq District Irrigation Department",
                "Assigned primary lead"
        );
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/assign")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INVESTIGATING"));

        // 5. Propose Corrective Action
        ProposeActionRequest actionReq = new ProposeActionRequest(
                "Geotextile Liner Patch",
                "Apply composite liner to damaged sector",
                "Eliminate seepage within 48h",
                operator.getId(),
                "Operator Incharge",
                "HIGH",
                Instant.now().plusSeconds(86400),
                "Zero visual leakage",
                "Onsite inspection"
        );

        String actionRes = mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/actions")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROPOSED"))
                .andReturn().getResponse().getContentAsString();

        UUID actionId = UUID.fromString(objectMapper.readTree(actionRes).path("data").path("id").asText());

        // 6. Approve Action
        ApproveActionRequest approveReq = new ApproveActionRequest(actionId, "Approved by regional director");
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/approve-action")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // 6b. Complete Action -> Transitions incident to PENDING_VERIFICATION
        com.aquaai.accountability.api.dto.CompleteActionRequest completeReq =
                new com.aquaai.accountability.api.dto.CompleteActionRequest(actionId, "Liner patch applied, water tested");
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/complete-action")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // 7. Separation of Duties Check: Action owner trying to verify without administrative override fails
        VerifyResolutionRequest selfVerifyReq = new VerifyResolutionRequest(
                actionId,
                "ONSITE_PHYSICAL_INSPECTION",
                VerificationOutcome.VERIFIED_RESOLVED,
                "I fixed it myself and verify myself",
                null,
                false
        );
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/verify")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfVerifyReq)))
                .andExpect(status().isBadRequest());

        // 8. Independent Verification by another user (Admin) succeeds
        User admin = createTestUser(UserRole.ADMIN);
        String independentVerifierToken = createToken(admin);

        VerifyResolutionRequest independentVerifyReq = new VerifyResolutionRequest(
                actionId,
                "ONSITE_PHYSICAL_INSPECTION",
                VerificationOutcome.VERIFIED_RESOLVED,
                "Independently inspected by Admin. Liner patch sealed, zero seepage detected.",
                "/evidence/inspection-pass.pdf",
                false
        );
        mockMvc.perform(post("/api/v1/accountability/incidents/" + incidentId + "/verify")
                        .header("Authorization", "Bearer " + independentVerifierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(independentVerifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value("VERIFIED_RESOLVED"));

        // 9. Incident is now RESOLVED
        mockMvc.perform(get("/api/v1/accountability/incidents/" + incidentId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.incident.status").value("RESOLVED"));
    }
}
