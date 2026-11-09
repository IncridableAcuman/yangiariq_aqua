package com.aquaai.admin.api;

import com.aquaai.admin.application.dto.AdminDtos.*;
import com.aquaai.admin.domain.repository.SecurityAuditLogRepository;
import com.aquaai.field.domain.model.*;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.api.dto.AdminCreateUserRequest;
import com.aquaai.user.api.dto.ChangeUserRoleRequest;
import com.aquaai.user.api.dto.ChangeUserStatusRequest;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminRbacSecurityIntegrationTest {

    private static final AtomicLong USER_SEQ = new AtomicLong(System.currentTimeMillis() % 1000000 + 8800000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private SecurityAuditLogRepository securityAuditLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private record TestUser(String token, UUID userId, User user) {}

    private TestUser createTestUser(UserRole role, UserStatus status) {
        long seq = USER_SEQ.incrementAndGet();
        String phone = "+99890" + seq;
        String email = "rbac_test_" + seq + "@aquaai.uz";

        User user = User.builder()
                .firstName("RBAC")
                .lastName("Tester" + seq)
                .fullName("RBAC Tester " + seq)
                .phoneNumber(phone)
                .email(email)
                .passwordHash("$2a$10$wK1.V/fJg80gW6gX9/649.336/7b3223687352372") // bcrypt dummy
                .role(role)
                .status(status)
                .district("Yangiariq")
                .build();
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateAccessToken(saved.getId(), saved.getEmail(), saved.getRole());
        return new TestUser("Bearer " + token, saved.getId(), saved);
    }

    private TestUser createTestUser(UserRole role) {
        return createTestUser(role, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Scenario 1: Unauthenticated access returns 401")
    void scenario01_unauthenticatedAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/roles"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 2: Authenticated but unauthorized access returns 403")
    void scenario02_unauthorizedAccessReturns403() throws Exception {
        TestUser farmer = createTestUser(UserRole.FARMER);

        mockMvc.perform(get("/api/v1/admin/roles")
                        .header("Authorization", farmer.token()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", farmer.token()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/settings")
                        .header("Authorization", farmer.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 3: SUPER_ADMIN can perform authorized platform administration")
    void scenario03_superAdminCanPerformPlatformAdministration() throws Exception {
        TestUser superAdmin = createTestUser(UserRole.SUPER_ADMIN);

        // 1. Can view roles
        mockMvc.perform(get("/api/v1/admin/roles")
                        .header("Authorization", superAdmin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 2. Can inspect system settings
        mockMvc.perform(get("/api/v1/admin/settings")
                        .header("Authorization", superAdmin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationName").exists())
                .andExpect(jsonPath("$.data.physicalActuationProhibited").value(true));

        // 3. Can create a custom role
        String customCode = "CUSTOM_AUDITOR_" + System.currentTimeMillis() % 10000;
        CreateRoleRequest createRoleReq = new CreateRoleRequest(
                customCode,
                "Canal Auditor",
                "Custom role for external water auditors",
                Set.of("REPORT_VIEW", "WATER_ALLOCATION_VIEW")
        );
        mockMvc.perform(post("/api/v1/admin/roles")
                        .header("Authorization", superAdmin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value(customCode));
    }

    @Test
    @DisplayName("Scenario 4: ADMIN cannot perform SUPER_ADMIN-only operations")
    void scenario04_adminCannotPerformSuperAdminOnlyOperations() throws Exception {
        TestUser admin = createTestUser(UserRole.ADMIN);

        // ADMIN cannot create a custom role (SUPER_ADMIN only)
        CreateRoleRequest createRoleReq = new CreateRoleRequest(
                "ILLEGAL_ROLE_" + System.currentTimeMillis() % 10000,
                "Illegal Role",
                "Should fail",
                Set.of("REPORT_VIEW")
        );
        mockMvc.perform(post("/api/v1/admin/roles")
                        .header("Authorization", admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleReq)))
                .andExpect(status().is4xxClientError());

        // ADMIN cannot delete a role
        mockMvc.perform(delete("/api/v1/admin/roles/OPERATOR")
                        .header("Authorization", admin.token()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Scenario 5: OPERATOR cannot manage platform users")
    void scenario05_operatorCannotManagePlatformUsers() throws Exception {
        TestUser operator = createTestUser(UserRole.OPERATOR);
        TestUser target = createTestUser(UserRole.FARMER);

        ChangeUserRoleRequest roleReq = new ChangeUserRoleRequest(UserRole.OPERATOR);
        mockMvc.perform(patch("/api/v1/users/" + target.userId() + "/role")
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().is4xxClientError());

        ChangeUserStatusRequest statusReq = new ChangeUserStatusRequest(UserStatus.BLOCKED, "Operator test");
        mockMvc.perform(patch("/api/v1/users/" + target.userId() + "/status")
                        .header("Authorization", operator.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Scenario 6: FARMER cannot access another farmer's resources")
    void scenario06_farmerCannotAccessAnotherFarmersResources() throws Exception {
        TestUser farmer1 = createTestUser(UserRole.FARMER);
        TestUser farmer2 = createTestUser(UserRole.FARMER);

        // Farmer 2 owns parcel
        Field field = Field.create(
                farmer2.userId(),
                "Farmer2 Secret Field",
                "Description",
                new BigDecimal("5.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                new FieldLocation(new BigDecimal("41.34"), new BigDecimal("60.60")),
                null
        );
        field.activate();
        Field savedField = fieldRepository.save(field);

        // Farmer 1 attempts to access Farmer 2's water accounting resource (Anti-IDOR)
        mockMvc.perform(get("/api/v1/water-accounting/fields/" + savedField.getId())
                        .header("Authorization", farmer1.token()))
                .andExpect(status().isForbidden());

        // Farmer 1 attempts to access Farmer 2's profile
        mockMvc.perform(get("/api/v1/users/" + farmer2.userId())
                        .header("Authorization", farmer1.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 7: Users cannot modify their own role to gain privileges")
    void scenario07_usersCannotModifyOwnRole() throws Exception {
        TestUser farmer = createTestUser(UserRole.FARMER);

        ChangeUserRoleRequest roleReq = new ChangeUserRoleRequest(UserRole.ADMIN);
        mockMvc.perform(patch("/api/v1/users/" + farmer.userId() + "/role")
                        .header("Authorization", farmer.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 8: ADMIN cannot assign privileges beyond their authority (cannot promote to SUPER_ADMIN)")
    void scenario08_adminCannotAssignSuperAdminPrivileges() throws Exception {
        TestUser admin = createTestUser(UserRole.ADMIN);
        TestUser target = createTestUser(UserRole.OPERATOR);

        ChangeUserRoleRequest roleReq = new ChangeUserRoleRequest(UserRole.SUPER_ADMIN);
        mockMvc.perform(patch("/api/v1/users/" + target.userId() + "/role")
                        .header("Authorization", admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Scenario 9: ADMIN cannot modify SUPER_ADMIN accounts")
    void scenario09_adminCannotModifySuperAdminAccounts() throws Exception {
        TestUser superAdmin = createTestUser(UserRole.SUPER_ADMIN);
        TestUser admin = createTestUser(UserRole.ADMIN);

        ChangeUserStatusRequest statusReq = new ChangeUserStatusRequest(UserStatus.BLOCKED, "Attempted admin block");
        mockMvc.perform(patch("/api/v1/users/" + superAdmin.userId() + "/status")
                        .header("Authorization", admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(delete("/api/v1/users/" + superAdmin.userId())
                        .header("Authorization", admin.token()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Scenario 10: The last active SUPER_ADMIN cannot be deactivated")
    void scenario10_lastActiveSuperAdminCannotBeDeactivated() throws Exception {
        // Ensure there is only 1 active super admin for this test
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.SUPER_ADMIN)
                .forEach(u -> {
                    u.changeStatus(UserStatus.INACTIVE);
                    userRepository.save(u);
                });

        TestUser loneSuperAdmin = createTestUser(UserRole.SUPER_ADMIN);

        // Attempt self-deactivation
        mockMvc.perform(post("/api/v1/users/profile/deactivate")
                        .header("Authorization", loneSuperAdmin.token()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Scenario 11: Inactive users cannot access protected endpoints")
    void scenario11_inactiveUsersCannotAccessProtectedEndpoints() throws Exception {
        TestUser inactiveUser = createTestUser(UserRole.OPERATOR, UserStatus.INACTIVE);

        // Token generated, but user in database is INACTIVE -> filter denies authentication
        mockMvc.perform(get("/api/v1/users/profile")
                        .header("Authorization", inactiveUser.token()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 12: Administrative provisioning creates user with encrypted password and proper role")
    void scenario12_adminUserProvisioning() throws Exception {
        TestUser superAdmin = createTestUser(UserRole.SUPER_ADMIN);

        String newPhone = "+99893" + (System.currentTimeMillis() % 1000000 + 100000);
        AdminCreateUserRequest req = new AdminCreateUserRequest(
                "Olim",
                "Agro",
                newPhone,
                "olim.agro@yangiariq.uz",
                "SecretPass123!",
                UserRole.OPERATOR,
                UserStatus.ACTIVE,
                "Yangiariq",
                "Suvchi MCHJ",
                "@olim_agro",
                "uz"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", superAdmin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("OPERATOR"))
                .andExpect(jsonPath("$.data.phoneNumber").value(newPhone))
                .andExpect(jsonPath("$.data.password").doesNotExist()) // Never returns password
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID newUserId = UUID.fromString(json.path("data").path("id").asText());
        User createdUser = userRepository.findById(newUserId).orElseThrow();
        assertThat(createdUser.getPasswordHash()).startsWith("$2a$"); // BCrypt hashed
    }

    @Test
    @DisplayName("Scenario 13 & 14: Audit logs are generated for sensitive operations and never expose credentials")
    void scenario13And14_auditLogsGeneratedAndCredentialsProtected() throws Exception {
        TestUser superAdmin = createTestUser(UserRole.SUPER_ADMIN);
        TestUser target = createTestUser(UserRole.FARMER);

        // Change status to trigger audit log
        ChangeUserStatusRequest statusReq = new ChangeUserStatusRequest(UserStatus.BLOCKED, "Audit test block");
        mockMvc.perform(patch("/api/v1/users/" + target.userId() + "/status")
                        .header("Authorization", superAdmin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk());

        // Inspect audit logs endpoint
        MvcResult auditResult = mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", superAdmin.token())
                        .param("action", "USER_STATUS_CHANGED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andReturn();

        String auditResponse = auditResult.getResponse().getContentAsString();
        assertThat(auditResponse).contains("USER_STATUS_CHANGED");
        assertThat(auditResponse).doesNotContain("passwordHash");
        assertThat(auditResponse).doesNotContain("SecretPass");
    }
}
