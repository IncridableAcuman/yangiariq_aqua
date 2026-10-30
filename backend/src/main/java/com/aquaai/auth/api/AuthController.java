package com.aquaai.auth.api;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.ChangePasswordRequest;
import com.aquaai.auth.api.dto.LoginRequest;
import com.aquaai.auth.api.dto.LogoutRequest;
import com.aquaai.auth.api.dto.PasswordResetConfirmRequest;
import com.aquaai.auth.api.dto.PasswordResetRequest;
import com.aquaai.auth.api.dto.RefreshTokenRequest;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.auth.application.AuthService;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token rotation, and password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new farmer account with default role FARMER")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {
        String deviceInfo = servletRequest.getHeader("User-Agent");
        String ipAddress = resolveClientIp(servletRequest);
        AuthResponse response = authService.register(request, deviceInfo, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user via phone or email and issue tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        String deviceInfo = servletRequest.getHeader("User-Agent");
        String ipAddress = resolveClientIp(servletRequest);
        AuthResponse response = authService.login(request, deviceInfo, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue new short-lived access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest) {
        String deviceInfo = servletRequest.getHeader("User-Agent");
        String ipAddress = resolveClientIp(servletRequest);
        AuthResponse response = authService.refreshToken(request, deviceInfo, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke current refresh token session")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) LogoutRequest request) {
        String tokenToRevoke = request != null ? request.refreshToken() : null;
        authService.logout(tokenToRevoke);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for authenticated user and revoke active sessions")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        authService.changePassword(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Initiate password reset workflow (non-enumerating)")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirm password reset using single-use verification token")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
