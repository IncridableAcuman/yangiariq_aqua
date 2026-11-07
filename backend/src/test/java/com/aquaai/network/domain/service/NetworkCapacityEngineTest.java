package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.NetworkCapacity;
import com.aquaai.network.domain.model.OperationalStatus;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkCapacityEngineTest {

    private final NetworkCapacityEngine engine = new NetworkCapacityEngine();

    @Test
    @DisplayName("Computes remaining capacity and utilization accurately")
    void computesCapacityAndUtilization() {
        WaterNetworkEdge edge = WaterNetworkEdge.builder()
                .id(UUID.randomUUID())
                .name("Test Canal")
                .code("TC")
                .type(WaterNetworkEdgeType.SECONDARY_CANAL)
                .startNodeId(UUID.randomUUID())
                .endNodeId(UUID.randomUUID())
                .maxCapacityM3S(new BigDecimal("5.0000"))
                .currentFlowM3S(new BigDecimal("2.0000"))
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .build();

        NetworkCapacity capacity = engine.evaluateSegmentCapacity(
                edge,
                new BigDecimal("1.0000"), // Planned additional flow
                false,
                null
        );

        assertThat(capacity.maxCapacityM3S()).isEqualByComparingTo("5.0000");
        assertThat(capacity.currentFlowM3S()).isEqualByComparingTo("2.0000");
        assertThat(capacity.plannedFlowM3S()).isEqualByComparingTo("1.0000");
        assertThat(capacity.remainingCapacityM3S()).isEqualByComparingTo("2.0000");
        assertThat(capacity.utilizationPercent()).isEqualByComparingTo("60.00");
        assertThat(capacity.isOverloaded()).isFalse();
        assertThat(capacity.constraintStatus()).isEqualTo("SAFE");
    }

    @Test
    @DisplayName("Hydraulic safety interlock strictly overrides status to BLOCKED")
    void hydraulicInterlockOverridesToBlocked() {
        WaterNetworkEdge edge = WaterNetworkEdge.builder()
                .id(UUID.randomUUID())
                .name("Test Canal")
                .code("TC")
                .type(WaterNetworkEdgeType.SECONDARY_CANAL)
                .startNodeId(UUID.randomUUID())
                .endNodeId(UUID.randomUUID())
                .maxCapacityM3S(new BigDecimal("5.0000"))
                .currentFlowM3S(BigDecimal.ZERO)
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .build();

        NetworkCapacity capacity = engine.evaluateSegmentCapacity(
                edge,
                BigDecimal.ZERO,
                true, // active burst or critical pressure drop
                "Downstream active pipe burst"
        );

        assertThat(capacity.constraintStatus()).isEqualTo("BLOCKED");
    }
}
