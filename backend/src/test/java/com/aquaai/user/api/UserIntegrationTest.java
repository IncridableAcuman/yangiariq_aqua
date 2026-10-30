package com.aquaai.user.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.user.api.dto.UpdateUserProfileRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse registerUser(String phone, String email) throws Exception {
        RegisterRequest register = RegisterRequest.builder()
                .firstName("Temur")
                .lastName("Bek")
                .phoneNumber(phone)
                .email(email)
                .password("TemurPass123!")
                .district("Yangiariq")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
    }

    @Test
    @DisplayName("Unauthenticated profile retrieval should return 401 Unauthorized")
    void testGetProfileUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("Authenticated user should view and update their profile")
    void testGetAndUpdateProfile() throws Exception {
        String phone = "+99893" + System.currentTimeMillis() % 10000000;
        String email = "temur" + System.currentTimeMillis() + "@yangiariq.uz";

        AuthResponse auth = registerUser(phone, email);

        // 1. Get profile
        mockMvc.perform(get("/api/v1/user/profile")
                        .header("Authorization", "Bearer " + auth.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phoneNumber").value(phone))
                .andExpect(jsonPath("$.data.firstName").value("Temur"))
                .andExpect(jsonPath("$.data.role").value("FARMER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // 2. Update profile
        UpdateUserProfileRequest updateRequest = UpdateUserProfileRequest.builder()
                .firstName("Amir Temur")
                .lastName("Kogon")
                .email("amirtemur" + System.currentTimeMillis() + "@yangiariq.uz")
                .district("Yangiariq Markaz")
                .build();

        mockMvc.perform(patch("/api/v1/user/profile")
                        .header("Authorization", "Bearer " + auth.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Amir Temur"))
                .andExpect(jsonPath("$.data.lastName").value("Kogon"))
                .andExpect(jsonPath("$.data.fullName").value("Amir Temur Kogon"))
                .andExpect(jsonPath("$.data.district").value("Yangiariq Markaz"));
    }

    @Test
    @DisplayName("Updating profile with an email already taken by another user should return 409 Conflict")
    void testUpdateProfileDuplicateEmail() throws Exception {
        String phone1 = "+99894" + (System.currentTimeMillis() % 10000000);
        String email1 = "user1_" + System.currentTimeMillis() + "@yangiariq.uz";
        registerUser(phone1, email1);

        String phone2 = "+99895" + (System.currentTimeMillis() % 10000000);
        String email2 = "user2_" + System.currentTimeMillis() + "@yangiariq.uz";
        AuthResponse auth2 = registerUser(phone2, email2);

        // User 2 attempts to claim User 1's email
        UpdateUserProfileRequest conflictUpdate = UpdateUserProfileRequest.builder()
                .firstName("UserTwo")
                .lastName("LastName")
                .email(email1)
                .build();

        mockMvc.perform(patch("/api/v1/user/profile")
                        .header("Authorization", "Bearer " + auth2.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictUpdate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }
}
