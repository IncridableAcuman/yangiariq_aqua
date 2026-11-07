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
@Table(name = "resilience_scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResilienceScenarioJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "scenario_type", nullable = false, length = 64)
    private String scenarioType;

    @Column(name = "disabled_node_ids_json", columnDefinition = "TEXT")
    private String disabledNodeIdsJson;

    @Column(name = "disabled_edge_ids_json", columnDefinition = "TEXT")
    private String disabledEdgeIdsJson;

    @Column(name = "capacity_reduction_percent", precision = 5, scale = 2)
    private BigDecimal capacityReductionPercent;

    @Column(name = "demand_surge_percent", precision = 5, scale = 2)
    private BigDecimal demandSurgePercent;

    @Column(name = "affected_field_count")
    private int affectedFieldCount;

    @Column(name = "lost_capacity_m3_s", precision = 8, scale = 4)
    private BigDecimal lostCapacityM3S;

    @Column(name = "unmet_demand_m3", precision = 12, scale = 2)
    private BigDecimal unmetDemandM3;

    @Column(name = "affected_wua_names_json", columnDefinition = "TEXT")
    private String affectedWuaNamesJson;

    @Column(name = "alternative_feasible_routes_count")
    private int alternativeFeasibleRoutesCount;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "simulation_results_json", columnDefinition = "TEXT")
    private String simulationResultsJson;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
