package com.aquaai.user.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.user.api.dto.UpdateUserProfileRequest;
import com.aquaai.user.api.dto.UserProfileResponse;
import com.aquaai.user.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user profile and identity management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile() {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserProfileResponse profile = userService.getProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update authenticated user profile information")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserProfileResponse updatedProfile = userService.updateProfile(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.ok(updatedProfile));
    }
}
