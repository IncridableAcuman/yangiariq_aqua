package com.aquaai.network.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Node within the regional water distribution graph.
 */
@Getter
@Builder(toBuilder = true)
public class WaterNetworkNode {

    private final UUID id;
    private final String name;
    private final String code;
    private final WaterNetworkNodeType type;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final BigDecimal elevationMeters;
    private final UUID waterSourceId;
    private final UUID fieldId;
    private final UUID wuaId;
    private boolean isActive;
    private final Map<String, Object> properties;
    private final Instant createdAt;
    private Instant updatedAt;

    public WaterNetworkNode(
            UUID id,
            String name,
            String code,
            WaterNetworkNodeType type,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal elevationMeters,
            UUID waterSourceId,
            UUID fieldId,
            UUID wuaId,
            boolean isActive,
            Map<String, Object> properties,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "Node name must not be null");
        this.code = Objects.requireNonNull(code, "Node code must not be null");
        this.type = Objects.requireNonNull(type, "Node type must not be null");
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevationMeters = elevationMeters != null ? elevationMeters : BigDecimal.ZERO;
        this.waterSourceId = waterSourceId;
        this.fieldId = fieldId;
        this.wuaId = wuaId;
        this.isActive = isActive;
        this.properties = properties != null ? Collections.unmodifiableMap(properties) : Collections.emptyMap();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = Instant.now();
    }
}
