package com.aquaai.user.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.api.dto.AdminCreateUserRequest;
import com.aquaai.user.api.dto.AdminUpdateUserRequest;
import com.aquaai.user.api.dto.ChangeUserRoleRequest;
import com.aquaai.user.api.dto.ChangeUserStatusRequest;
import com.aquaai.user.api.dto.UpdateUserProfileRequest;
import com.aquaai.user.api.dto.UserProfileResponse;
import com.aquaai.user.api.dto.UserSummaryStatsResponse;
import com.aquaai.user.application.UserService;
import com.aquaai.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/users", "/api/v1/user", "/api/v1/admin/users"})
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user profile, directory search, and identity management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping({"/profile", "/me"})
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile() {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserProfileResponse profile = userService.getProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PatchMapping({"/profile", "/me"})
    @Operation(summary = "Update authenticated user profile information")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserProfileResponse updatedProfile = userService.updateProfile(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.ok(updatedProfile));
    }

    @PostMapping({"/profile/deactivate", "/me/deactivate"})
    @Operation(summary = "Self-deactivate authenticated user account")
    public ResponseEntity<ApiResponse<Void>> deactivateOwnAccount() {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        userService.deactivateOwnAccount(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "Get district-wide user summary metrics (Operator/Admin only)")
    public ResponseEntity<ApiResponse<UserSummaryStatsResponse>> getUserSummaryStats() {
        UserSummaryStatsResponse stats = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "List and search users directory with filtering and pagination (Operator/Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        PageResponse<UserProfileResponse> result = userService.getUsers(search, role, status, district, pageRequest);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Administrative provisioning of a new user account (Admin only)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        UserProfileResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID (Owner, Operator, or Admin)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable UUID id) {
        UserProfileResponse profile = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Full update of user account by administrator (Admin only)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> adminUpdateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        UserProfileResponse updated = userService.adminUpdateUser(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_ACTIVATE') or hasAuthority('USER_DEACTIVATE') or hasRole('ADMIN')")
    @Operation(summary = "Change user account status (Admin only)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> changeUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeUserStatusRequest request) {
        UserProfileResponse updated = userService.changeUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('USER_ROLE_ASSIGN') or hasRole('ADMIN')")
    @Operation(summary = "Change user role (Admin only)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> changeUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeUserRoleRequest request) {
        UserProfileResponse updated = userService.changeUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Soft-delete and block user account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
