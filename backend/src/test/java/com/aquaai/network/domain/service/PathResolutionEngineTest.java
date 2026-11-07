package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PathResolutionEngineTest {

    private final PathResolutionEngine engine = new PathResolutionEngine();

    @Test
    @DisplayName("Finds downstream fields dependent on a canal segment")
    void findsDownstreamFields() {
        UUID srcId = UUID.randomUUID();
        UUID jncId = UUID.randomUUID();
        UUID fld1InletId = UUID.randomUUID();
        UUID fld2InletId = UUID.randomUUID();

        UUID field1Id = UUID.randomUUID();
        UUID field2Id = UUID.randomUUID();

        WaterNetworkNode src = WaterNetworkNode.builder().id(srcId).name("Source").code("S").type(WaterNetworkNodeType.WATER_SOURCE).build();
        WaterNetworkNode jnc = WaterNetworkNode.builder().id(jncId).name("Junction").code("J").type(WaterNetworkNodeType.CANAL_JUNCTION).build();
        WaterNetworkNode in1 = WaterNetworkNode.builder().id(fld1InletId).name("Inlet 1").code("I1").type(WaterNetworkNodeType.FIELD_INLET).fieldId(field1Id).build();
        WaterNetworkNode in2 = WaterNetworkNode.builder().id(fld2InletId).name("Inlet 2").code("I2").type(WaterNetworkNodeType.FIELD_INLET).fieldId(field2Id).build();

        UUID mainEdgeId = UUID.randomUUID();
        WaterNetworkEdge mainEdge = WaterNetworkEdge.builder()
                .id(mainEdgeId).name("Main").code("M").type(WaterNetworkEdgeType.PRIMARY_CANAL)
                .startNodeId(srcId).endNodeId(jncId).maxCapacityM3S(new BigDecimal("10.0")).build();

        WaterNetworkEdge branch1 = WaterNetworkEdge.builder()
                .id(UUID.randomUUID()).name("B1").code("B1").type(WaterNetworkEdgeType.SECONDARY_CANAL)
                .startNodeId(jncId).endNodeId(fld1InletId).maxCapacityM3S(new BigDecimal("3.0")).build();

        WaterNetworkEdge branch2 = WaterNetworkEdge.builder()
                .id(UUID.randomUUID()).name("B2").code("B2").type(WaterNetworkEdgeType.SECONDARY_CANAL)
                .startNodeId(jncId).endNodeId(fld2InletId).maxCapacityM3S(new BigDecimal("3.0")).build();

        List<UUID> downstream = engine.findDownstreamFieldIds(
                mainEdgeId,
                List.of(src, jnc, in1, in2),
                List.of(mainEdge, branch1, branch2)
        );

        assertThat(downstream).containsExactlyInAnyOrder(field1Id, field2Id);
    }
}
