package com.aquaai.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record RoleResponse(
            UUID id,
            String code,
            String name,
            String description,
            boolean isSystem,
            Set<String> permissions,
            long userCount,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CreateRoleRequest(
            @NotBlank(message = "Role code is required")
            String code,

            @NotBlank(message = "Role name is required")
            String name,

            String description,

            @NotEmpty(message = "At least one permission must be assigned")
            Set<String> permissions
    ) {}

    public record UpdateRoleDetailsRequest(
            String name,
            String description
    ) {}

    public record UpdateRolePermissionsRequest(
            @NotEmpty(message = "Role must retain at least one permission")
            Set<String> permissions
    ) {}

    public record PermissionResponse(
            UUID id,
            String code,
            String name,
            String description,
            String category
    ) {}

    public record PermissionCategoryGroup(
            String category,
            String categoryLabel,
            List<PermissionResponse> permissions
    ) {}

    public record SecurityAuditLogResponse(
            UUID id,
            UUID actorId,
            String actorName,
            String actorRole,
            String action,
            String targetType,
            String targetId,
            String detailsJson,
            String result,
            String ipAddress,
            Instant createdAt
    ) {}

    public record SystemSettingsResponse(
            String applicationName,
            String environment,
            String version,
            long accessTokenExpirationMinutes,
            long refreshTokenExpirationDays,
            String activeSensorProvider,
            String weatherProvider,
            boolean rateLimitingEnabled,
            int rateLimitPerMinute,
            boolean fourEyesPrincipleStrict,
            boolean physicalActuationProhibited,
            Instant serverTime
    ) {}
}
