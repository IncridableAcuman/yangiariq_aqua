package com.aquaai.network.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "water_network_edges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterNetworkEdgeJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "edge_type", nullable = false, length = 64)
    private String edgeType;

    @Column(name = "start_node_id", nullable = false)
    private UUID startNodeId;

    @Column(name = "end_node_id", nullable = false)
    private UUID endNodeId;

    @Column(name = "wua_id")
    private UUID wuaId;

    @Column(name = "length_meters", precision = 10, scale = 2, nullable = false)
    private BigDecimal lengthMeters;

    @Column(name = "max_capacity_m3_s", precision = 8, scale = 4, nullable = false)
    private BigDecimal maxCapacityM3S;

    @Column(name = "min_flow_m3_s", precision = 8, scale = 4, nullable = false)
    private BigDecimal minFlowM3S;

    @Column(name = "current_flow_m3_s", precision = 8, scale = 4, nullable = false)
    private BigDecimal currentFlowM3S;

    @Column(name = "operational_status", nullable = false, length = 32)
    private String operationalStatus;

    @Column(name = "geometry_json", columnDefinition = "TEXT")
    private String geometryJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
