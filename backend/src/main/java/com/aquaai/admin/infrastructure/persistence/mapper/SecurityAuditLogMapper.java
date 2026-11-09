package com.aquaai.admin.infrastructure.persistence.mapper;

import com.aquaai.admin.domain.model.SecurityAuditLog;
import com.aquaai.admin.infrastructure.persistence.entity.SecurityAuditLogJpaEntity;

public final class SecurityAuditLogMapper {

    private SecurityAuditLogMapper() {
    }

    public static SecurityAuditLog toDomain(SecurityAuditLogJpaEntity entity) {
        if (entity == null) return null;
        return SecurityAuditLog.builder()
                .id(entity.getId())
                .actorId(entity.getActorId())
                .actorName(entity.getActorName())
                .actorRole(entity.getActorRole())
                .action(entity.getAction())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .detailsJson(entity.getDetailsJson())
                .result(entity.getResult())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static SecurityAuditLogJpaEntity toEntity(SecurityAuditLog domain) {
        if (domain == null) return null;
        return SecurityAuditLogJpaEntity.builder()
                .id(domain.getId())
                .actorId(domain.getActorId())
                .actorName(domain.getActorName())
                .actorRole(domain.getActorRole())
                .action(domain.getAction())
                .targetType(domain.getTargetType())
                .targetId(domain.getTargetId())
                .detailsJson(domain.getDetailsJson())
                .result(domain.getResult())
                .ipAddress(domain.getIpAddress())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
