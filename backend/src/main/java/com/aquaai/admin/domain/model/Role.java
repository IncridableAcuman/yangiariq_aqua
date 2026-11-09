package com.aquaai.admin.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Role {

    private final UUID id;
    private final String code;
    private String name;
    private String description;
    private final boolean isSystem;
    @Builder.Default
    private Set<String> permissions = new HashSet<>();
    private final Instant createdAt;
    private Instant updatedAt;

    public static Role createCustom(String code, String name, String description, Set<String> initialPermissions) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code cannot be empty");
        }
        String normalizedCode = code.trim().toUpperCase();
        if (normalizedCode.startsWith("ROLE_")) {
            normalizedCode = normalizedCode.substring(5);
        }
        Instant now = Instant.now();
        return Role.builder()
                .id(UUID.randomUUID())
                .code(normalizedCode)
                .name(name != null && !name.isBlank() ? name.trim() : normalizedCode)
                .description(description != null ? description.trim() : null)
                .isSystem(false)
                .permissions(initialPermissions != null ? new HashSet<>(initialPermissions) : new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateDetails(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        this.updatedAt = Instant.now();
    }

    public void updatePermissions(Set<String> newPermissions) {
        if (this.isSystem && "SUPER_ADMIN".equalsIgnoreCase(this.code)) {
            throw new IllegalStateException("Cannot revoke permissions from the SUPER_ADMIN system role");
        }
        this.permissions = new HashSet<>(Objects.requireNonNullElseGet(newPermissions, HashSet::new));
        this.updatedAt = Instant.now();
    }
}
