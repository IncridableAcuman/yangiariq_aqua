package com.aquaai.admin.api;

import com.aquaai.admin.application.dto.AdminDtos.SystemSettingsResponse;
import com.aquaai.admin.application.service.SystemSettingsService;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Platform Settings", description = "Endpoints for inspecting platform configuration and runtime policies")
@SecurityRequirement(name = "bearerAuth")
public class AdminSettingsController {

    private final SystemSettingsService systemSettingsService;

    @GetMapping
    @PreAuthorize("hasAuthority('SYSTEM_SETTINGS_VIEW') or hasRole('ADMIN')")
    @Operation(summary = "Get current platform environment settings and runtime flags")
    public ResponseEntity<ApiResponse<SystemSettingsResponse>> getSystemSettings() {
        SystemSettingsResponse settings = systemSettingsService.getSystemSettings();
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }
}
