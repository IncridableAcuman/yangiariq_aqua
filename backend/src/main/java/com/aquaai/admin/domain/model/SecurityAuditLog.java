package com.aquaai.admin.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class SecurityAuditLog {

    private final UUID id;
    private final UUID actorId;
    private final String actorName;
    private final String actorRole;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final String detailsJson;
    private final String result;
    private final String ipAddress;
    private final Instant createdAt;

    public static SecurityAuditLog create(
            UUID actorId,
            String actorName,
            String actorRole,
            String action,
            String targetType,
            String targetId,
            String detailsJson,
            String result,
            String ipAddress
    ) {
        return SecurityAuditLog.builder()
                .id(UUID.randomUUID())
                .actorId(actorId)
                .actorName(actorName)
                .actorRole(actorRole)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detailsJson(detailsJson)
                .result(result != null ? result : "SUCCESS")
                .ipAddress(ipAddress)
                .createdAt(Instant.now())
                .build();
    }
}
