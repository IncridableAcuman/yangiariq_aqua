package com.aquaai.pilot.api;

import com.aquaai.field.domain.model.*;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.pilot.application.dto.PilotDtos.*;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PilotIntegrationTest {

    private static final AtomicLong USER_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000 + 9200000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private record TestUser(String token, UUID userId) {}

    private TestUser createTestUser(UserRole role) {
        long seq = USER_COUNTER.incrementAndGet();
        String phone = "+998" + seq;
        String email = "pilot_user_" + seq + "@aquaai.uz";

        User user = User.builder()
                .firstName("Pilot")
                .lastName("Tester")
                .fullName("Pilot Tester " + seq)
                .phoneNumber(phone)
                .email(email)
                .passwordHash("hashedPass123")
                .role(role)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build();
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateAccessToken(saved.getId(), saved.getEmail(), saved.getRole());
        return new TestUser("Bearer " + token, saved.getId());
    }

    private UUID createTestField(UUID ownerId) {
        Field field = Field.create(
                ownerId,
                "Pilot Parcel " + System.currentTimeMillis() % 10000,
                "Experimental drip parcel in Yangiariq",
                new BigDecimal("4.5"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                new FieldLocation(new BigDecimal("41.3431"), new BigDecimal("60.6033")),
                null
        );
        field.activate();
        Field saved = fieldRepository.save(field);
        return saved.getId();
    }

    @Test
    @DisplayName("Complete Pilot Lifecycle: Program Creation, Readiness Assessment, Four-Eyes Ground Truth, Prediction Validation, and Evaluation Reporting")
    void testFullPilotWorkflow() throws Exception {
        TestUser operator = createTestUser(UserRole.OPERATOR);
        TestUser independentVerifier = createTestUser(UserRole.OPERATOR);
        UUID fieldId = createTestField(operator.userId());

        // 1. Create Pilot Program
        String pilotCode = "PILOT-IT-" + System.currentTimeMillis() % 100000;
        CreatePilotRequest createReq = new CreatePilotRequest(
                pilotCode,
                "Khorezm Cotton Water Efficiency Pilot",
                "Operational pilot monitoring drip efficiency and soil moisture prediction",
                "Yangiariq Water District Sector 3",
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "Yangiariq WUA",
                "Validate 15% conveyance conservation and predictive precision"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/pilot-programs")
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value(pilotCode))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        UUID pilotId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Enroll Participant
        AddParticipantRequest addPartReq = new AddParticipantRequest(
                operator.userId(),
                "FIELD_LEAD",
                fieldId.toString()
        );
        mockMvc.perform(post("/api/v1/pilot-programs/" + pilotId + "/participants")
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addPartReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roleInPilot").value("FIELD_LEAD"));

        // 3. Enroll Field Parcel
        AddFieldRequest addFieldReq = new AddFieldRequest(
                fieldId,
                "Soil moisture dynamics and turnout delivery",
                "Daily TDR sampling at parcel midpoint; continuous canal intake monitoring",
                "Sensor readings within 5% of gravimetric baseline"
        );
        mockMvc.perform(post("/api/v1/pilot-programs/" + pilotId + "/fields")
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addFieldReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.monitoringTarget").exists());

        // 4. Approve Pilot Program
        ApprovePilotRequest approveReq = new ApprovePilotRequest("Approved by regional water basin inspection committee");
        mockMvc.perform(post("/api/v1/pilot-programs/" + pilotId + "/approve")
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // 5. Assess Readiness
        mockMvc.perform(post("/api/v1/pilot-readiness/" + pilotId + "/assess")
                        .header("Authorization", operator.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checks").isArray())
                .andExpect(jsonPath("$.data.overallStatus").exists());

        // 6. Activate Pilot Program
        mockMvc.perform(post("/api/v1/pilot-programs/" + pilotId + "/activate")
                        .header("Authorization", operator.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // 7. Submit Ground-Truth Measurement
        SubmitGroundTruthRequest gtReq = new SubmitGroundTruthRequest(
                fieldId,
                "SOIL_MOISTURE",
                46.8,
                "%",
                Instant.now(),
                "PORTABLE_TDR_PROBE",
                "Field Inspection Team Alpha",
                "CERT-TDR-CALIB-2026",
                "Midday sampling at 30cm root zone depth"
        );

        MvcResult gtResult = mockMvc.perform(post("/api/v1/pilot-ground-truth/pilot/" + pilotId)
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gtReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.verificationStatus").value("SUBMITTED"))
                .andReturn();

        UUID gtId = UUID.fromString(objectMapper.readTree(gtResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 8. Attempt Self-Verification (Four-Eyes Violation -> Expect 400 Bad Request or Client Error)
        VerifyGroundTruthRequest selfVerifyReq = new VerifyGroundTruthRequest(
                "VERIFY",
                "PHOTO-EVIDENCE-01",
                "Attempting to verify my own reading",
                null
        );
        mockMvc.perform(post("/api/v1/pilot-ground-truth/" + gtId + "/review")
                        .header("Authorization", operator.token()) // same user as submitter!
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfVerifyReq)))
                .andExpect(status().is4xxClientError());

        // 9. Independent Four-Eyes Verification (Succeeds)
        VerifyGroundTruthRequest validVerifyReq = new VerifyGroundTruthRequest(
                "VERIFY",
                "CERT-TDR-CALIB-2026-ATTESTED",
                "Independently inspected against calibrated core probe",
                null
        );
        mockMvc.perform(post("/api/v1/pilot-ground-truth/" + gtId + "/review")
                        .header("Authorization", independentVerifier.token()) // distinct user!
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        // 10. Prediction Validation vs Verified Ground Truth
        EvaluatePredictionRequest predReq = new EvaluatePredictionRequest(
                fieldId,
                "PRED-SM-2026-99",
                "HybridSoilMoistureModel",
                "v2.1.0",
                Instant.now().minusSeconds(3600 * 12),
                12,
                45.0, // predicted 45.0%
                46.8, // actual was 46.8%
                gtId  // verified ground truth
        );
        mockMvc.perform(post("/api/v1/pilot-validation/pilot/" + pilotId)
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(predReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.evaluationStatus").value("VALIDATED"))
                .andExpect(jsonPath("$.data.absoluteError").value(1.8));

        // 11. Submit Field Operator Feedback
        SubmitFeedbackRequest feedbackReq = new SubmitFeedbackRequest(
                null,
                fieldId,
                null,
                null,
                null,
                "IRRIGATION_OPERATION",
                "Canal intake gate gravel buildup",
                "Observed localized silt accumulation at turnout inlet pipe reducing gravity flow"
        );
        mockMvc.perform(post("/api/v1/pilot-feedback/pilot/" + pilotId)
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        // 12. Retrieve Operational Monitoring KPIs
        mockMvc.perform(get("/api/v1/pilot-evaluations/pilot/" + pilotId + "/monitoring")
                        .header("Authorization", operator.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalGroundTruthMeasurements").value(1))
                .andExpect(jsonPath("$.data.verifiedGroundTruths").value(1))
                .andExpect(jsonPath("$.data.totalPredictionValidations").value(1));

        // 13. Generate Pilot Evaluation Report Snapshot
        GenerateEvaluationReportRequest reportReq = new GenerateEvaluationReportRequest(
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "Pilot evaluated with verified handheld probe sampling; IoT gateway awaiting hardware deployment.",
                "1. Maintain daily four-eyes sampling. 2. Integrate physical IoT gateways when installed."
        );
        mockMvc.perform(post("/api/v1/pilot-evaluations/pilot/" + pilotId + "/report")
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dataPerformanceJson").exists())
                .andExpect(jsonPath("$.data.predictionPerformanceJson").exists());
    }
}
