package com.aquaai.admin.infrastructure.persistence.mapper;

import com.aquaai.admin.domain.model.Permission;
import com.aquaai.admin.domain.model.Role;
import com.aquaai.admin.infrastructure.persistence.entity.PermissionJpaEntity;
import com.aquaai.admin.infrastructure.persistence.entity.RoleJpaEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleEntityMapper {

    private RoleEntityMapper() {
    }

    public static Permission toDomain(PermissionJpaEntity entity) {
        if (entity == null) return null;
        return Permission.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static PermissionJpaEntity toEntity(Permission domain) {
        if (domain == null) return null;
        return PermissionJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public static Role toDomain(RoleJpaEntity entity) {
        if (entity == null) return null;
        Set<String> permissionCodes = entity.getPermissions() != null
                ? entity.getPermissions().stream().map(PermissionJpaEntity::getCode).collect(Collectors.toSet())
                : new HashSet<>();

        return Role.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .isSystem(entity.isSystem())
                .permissions(permissionCodes)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static RoleJpaEntity toEntity(Role domain, Set<PermissionJpaEntity> permissionEntities) {
        if (domain == null) return null;
        return RoleJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .isSystem(domain.isSystem())
                .permissions(permissionEntities != null ? permissionEntities : new HashSet<>())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
