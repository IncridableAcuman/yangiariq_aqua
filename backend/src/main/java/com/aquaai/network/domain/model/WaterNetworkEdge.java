package com.aquaai.network.domain.model;

import com.aquaai.field.domain.model.CoordinatePoint;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Directed edge representing a physical conveyance canal, ditch, or pipeline
 * connecting two network nodes.
 */
@Getter
@Builder(toBuilder = true)
public class WaterNetworkEdge {

    private final UUID id;
    private final String name;
    private final String code;
    private final WaterNetworkEdgeType type;
    private final UUID startNodeId;
    private final UUID endNodeId;
    private final UUID wuaId;
    private final BigDecimal lengthMeters;
    private final BigDecimal maxCapacityM3S;
    private final BigDecimal minFlowM3S;
    private BigDecimal currentFlowM3S;
    private OperationalStatus operationalStatus;
    private final List<CoordinatePoint> geometry;
    private final Instant createdAt;
    private Instant updatedAt;

    public WaterNetworkEdge(
            UUID id,
            String name,
            String code,
            WaterNetworkEdgeType type,
            UUID startNodeId,
            UUID endNodeId,
            UUID wuaId,
            BigDecimal lengthMeters,
            BigDecimal maxCapacityM3S,
            BigDecimal minFlowM3S,
            BigDecimal currentFlowM3S,
            OperationalStatus operationalStatus,
            List<CoordinatePoint> geometry,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "Edge name must not be null");
        this.code = Objects.requireNonNull(code, "Edge code must not be null");
        this.type = Objects.requireNonNull(type, "Edge type must not be null");
        this.startNodeId = Objects.requireNonNull(startNodeId, "startNodeId must not be null");
        this.endNodeId = Objects.requireNonNull(endNodeId, "endNodeId must not be null");
        this.wuaId = wuaId;
        this.lengthMeters = lengthMeters != null ? lengthMeters : BigDecimal.ZERO;
        this.maxCapacityM3S = Objects.requireNonNull(maxCapacityM3S, "maxCapacityM3S must not be null");
        this.minFlowM3S = minFlowM3S != null ? minFlowM3S : BigDecimal.ZERO;
        this.currentFlowM3S = currentFlowM3S != null ? currentFlowM3S : BigDecimal.ZERO;
        this.operationalStatus = operationalStatus != null ? operationalStatus : OperationalStatus.OPERATIONAL;
        this.geometry = geometry != null ? Collections.unmodifiableList(new ArrayList<>(geometry)) : Collections.emptyList();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void updateCurrentFlow(BigDecimal flowM3S) {
        this.currentFlowM3S = flowM3S != null ? flowM3S : BigDecimal.ZERO;
        this.updatedAt = Instant.now();
    }

    public void setOperationalStatus(OperationalStatus status) {
        this.operationalStatus = Objects.requireNonNull(status, "Status must not be null");
        this.updatedAt = Instant.now();
    }
}
