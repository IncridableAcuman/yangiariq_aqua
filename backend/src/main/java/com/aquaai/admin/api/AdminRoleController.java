package com.aquaai.admin.api;

import com.aquaai.admin.application.dto.AdminDtos.*;
import com.aquaai.admin.application.service.RoleAndPermissionService;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Role & Permission Management", description = "Centralized platform RBAC management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminRoleController {

    private final RoleAndPermissionService roleAndPermissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('ADMIN')")
    @Operation(summary = "List all platform roles with assigned permissions and user counts")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles() {
        List<RoleResponse> roles = roleAndPermissionService.listRoles();
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('ADMIN')")
    @Operation(summary = "Get single role details and assigned permission codes")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable String code) {
        RoleResponse role = roleAndPermissionService.getRole(code);
        return ResponseEntity.ok(ApiResponse.ok(role));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create custom operational role (Super Admin only)")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse created = roleAndPermissionService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{code}/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_ASSIGN') or hasRole('ADMIN')")
    @Operation(summary = "Update permissions assigned to a role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRolePermissions(
            @PathVariable String code,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        RoleResponse updated = roleAndPermissionService.updateRolePermissions(code, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasAuthority('ROLE_DELETE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete custom operational role (Super Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String code) {
        roleAndPermissionService.deleteRole(code);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW') or hasRole('ADMIN')")
    @Operation(summary = "Get complete platform permission catalog")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> listPermissions() {
        List<PermissionResponse> permissions = roleAndPermissionService.listPermissions();
        return ResponseEntity.ok(ApiResponse.ok(permissions));
    }

    @GetMapping("/permissions/grouped")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW') or hasRole('ADMIN')")
    @Operation(summary = "Get platform permissions grouped by operational category")
    public ResponseEntity<ApiResponse<List<PermissionCategoryGroup>>> getPermissionsGrouped() {
        List<PermissionCategoryGroup> groups = roleAndPermissionService.getPermissionsGroupedByCategory();
        return ResponseEntity.ok(ApiResponse.ok(groups));
    }
}
