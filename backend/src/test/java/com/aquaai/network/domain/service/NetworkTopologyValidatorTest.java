package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.OperationalStatus;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkTopologyValidatorTest {

    private final NetworkTopologyValidator validator = new NetworkTopologyValidator();

    @Test
    @DisplayName("Valid connected network passes validation without errors")
    void validNetworkPasses() {
        UUID srcId = UUID.randomUUID();
        UUID pumpId = UUID.randomUUID();
        UUID inletId = UUID.randomUUID();

        WaterNetworkNode src = WaterNetworkNode.builder()
                .id(srcId).name("Source").code("SRC").type(WaterNetworkNodeType.WATER_SOURCE).build();
        WaterNetworkNode pump = WaterNetworkNode.builder()
                .id(pumpId).name("Pump").code("PMP").type(WaterNetworkNodeType.PUMP_STATION).build();
        WaterNetworkNode inlet = WaterNetworkNode.builder()
                .id(inletId).name("Inlet").code("INL").type(WaterNetworkNodeType.FIELD_INLET).build();

        WaterNetworkEdge e1 = WaterNetworkEdge.builder()
                .id(UUID.randomUUID()).name("E1").code("E1").type(WaterNetworkEdgeType.PRIMARY_CANAL)
                .startNodeId(srcId).endNodeId(pumpId).maxCapacityM3S(new BigDecimal("5.0")).build();
        WaterNetworkEdge e2 = WaterNetworkEdge.builder()
                .id(UUID.randomUUID()).name("E2").code("E2").type(WaterNetworkEdgeType.SECONDARY_CANAL)
                .startNodeId(pumpId).endNodeId(inletId).maxCapacityM3S(new BigDecimal("2.0")).build();

        var result = validator.validate(List.of(src, pump, inlet), List.of(e1, e2));
        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("Detects disconnected node in network")
    void detectsDisconnectedNode() {
        UUID srcId = UUID.randomUUID();
        UUID orphanId = UUID.randomUUID();

        WaterNetworkNode src = WaterNetworkNode.builder()
                .id(srcId).name("Source").code("SRC").type(WaterNetworkNodeType.WATER_SOURCE).build();
        WaterNetworkNode orphan = WaterNetworkNode.builder()
                .id(orphanId).name("Orphan").code("ORP").type(WaterNetworkNodeType.CANAL_JUNCTION).build();

        var result = validator.validate(List.of(src, orphan), Collections.emptyList());
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("disconnected node detected"));
    }

    @Test
    @DisplayName("Empty network returns invalid validation result")
    void emptyNetworkIsInvalid() {
        var result = validator.validate(Collections.emptyList(), Collections.emptyList());
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Water network contains zero nodes.");
    }
}
