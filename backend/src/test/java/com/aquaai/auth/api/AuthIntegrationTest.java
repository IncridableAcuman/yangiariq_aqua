package com.aquaai.auth.api;

import com.aquaai.auth.api.dto.ChangePasswordRequest;
import com.aquaai.auth.api.dto.LoginRequest;
import com.aquaai.auth.api.dto.LogoutRequest;
import com.aquaai.auth.api.dto.PasswordResetConfirmRequest;
import com.aquaai.auth.api.dto.PasswordResetRequest;
import com.aquaai.auth.api.dto.RefreshTokenRequest;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.auth.infrastructure.MockPasswordResetNotificationProvider;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockPasswordResetNotificationProvider mockPasswordResetNotificationProvider;

    @Test
    @DisplayName("Complete end-to-end authentication, token rotation, and reuse detection flow")
    void testEndToEndAuthAndTokenLifecycle() throws Exception {
        String phone = "+99890" + System.currentTimeMillis() % 10000000;
        String email = "farmer" + System.currentTimeMillis() + "@yangiariq.uz";

        // 1. Registration
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Rustam")
                .lastName("Karimov")
                .phoneNumber(phone)
                .email(email)
                .password("Password123!")
                .district("Yangiariq")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.user.role").value("FARMER"))
                .andReturn();

        // 2. Duplicate registration rejection
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));

        // 3. Login
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(phone)
                .password("Password123!")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode loginData = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("data");
        String accessToken = loginData.get("accessToken").asText();
        String initialRefreshToken = loginData.get("refreshToken").asText();

        // 4. Invalid Login (wrong password)
        LoginRequest invalidLogin = LoginRequest.builder()
                .identifier(phone)
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 5. Refresh token rotation
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(initialRefreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode refreshData = objectMapper.readTree(refreshResult.getResponse().getContentAsString()).get("data");
        String newRefreshToken = refreshData.get("refreshToken").asText();
        assertThat(newRefreshToken).isNotEqualTo(initialRefreshToken);

        // 6. Token reuse detection: Replaying the old rotated refresh token
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(initialRefreshToken))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TOKEN_REUSE_DETECTED"));

        // 7. Change password
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("Password123!")
                .newPassword("NewSuperSecretPassword456!")
                .build();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 8. Old password must now fail
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        // 9. New password must succeed
        LoginRequest newLoginRequest = LoginRequest.builder()
                .identifier(phone)
                .password("NewSuperSecretPassword456!")
                .build();

        MvcResult newLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String logoutRefreshToken = objectMapper.readTree(newLoginResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // 10. Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LogoutRequest(logoutRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Password reset workflow: request, notification dispatch, single-use confirmation")
    void testPasswordResetFlow() throws Exception {
        String phone = "+99891" + System.currentTimeMillis() % 10000000;
        String email = "reset" + System.currentTimeMillis() + "@yangiariq.uz";

        // Register user
        RegisterRequest register = RegisterRequest.builder()
                .firstName("Anvar")
                .lastName("Qodirov")
                .phoneNumber(phone)
                .email(email)
                .password("InitialPass123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Request reset
        PasswordResetRequest resetRequest = new PasswordResetRequest(email);
        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Retrieve raw token from mock notification provider
        String rawToken = mockPasswordResetNotificationProvider.getLatestTokenForTesting(email)
                .orElseThrow(() -> new AssertionError("Expected token in mock provider"));

        // Confirm reset
        PasswordResetConfirmRequest confirmRequest = PasswordResetConfirmRequest.builder()
                .token(rawToken)
                .newPassword("BrandNewResetPassword789!")
                .build();

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Reusing the same reset token must be rejected (single-use)
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("RESET_TOKEN_ALREADY_USED"));

        // Verify login works with the newly set password
        LoginRequest login = LoginRequest.builder()
                .identifier(phone)
                .password("BrandNewResetPassword789!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
