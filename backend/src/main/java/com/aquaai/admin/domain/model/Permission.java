package com.aquaai.admin.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Permission {

    private final UUID id;
    private final String code;
    private final String name;
    private final String description;
    private final String category;
    private final Instant createdAt;
}
