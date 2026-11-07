package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.HydraulicSurgeRisk;
import com.aquaai.network.domain.model.OperationalStatus;
import com.aquaai.network.domain.model.ResilienceScenario;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HydraulicResilienceEvaluatorTest {

    private final HydraulicResilienceEvaluator evaluator = new HydraulicResilienceEvaluator();

    @Test
    @DisplayName("Ramping rate > 0.50 m3/s/hr triggers abrupt surge warning")
    void detectsSurgeRampingRisk() {
        WaterNetworkEdge edge = WaterNetworkEdge.builder()
                .id(UUID.randomUUID())
                .name("Primary Canal")
                .code("PC")
                .type(WaterNetworkEdgeType.PRIMARY_CANAL)
                .startNodeId(UUID.randomUUID())
                .endNodeId(UUID.randomUUID())
                .maxCapacityM3S(new BigDecimal("5.0"))
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .build();

        // Flow change 1.5 m3/s over 1 hour = 1.5 m3/s/hr > 0.50
        HydraulicSurgeRisk risk = evaluator.evaluateSurgeRisk(
                edge,
                new BigDecimal("1.50"),
                BigDecimal.ONE
        );

        assertThat(risk.abruptChangeDetected()).isTrue();
        assertThat(risk.riskStatus()).isEqualTo("CONSTRAINED");
    }

    @Test
    @DisplayName("Simulates disruption scenario without mutating original nodes or edges")
    void simulatesDisruptionScenarioSafely() {
        UUID srcId = UUID.randomUUID();
        UUID distId = UUID.randomUUID();
        UUID inletId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        WaterNetworkNode src = WaterNetworkNode.builder().id(srcId).name("Src").code("S").type(WaterNetworkNodeType.WATER_SOURCE).build();
        WaterNetworkNode dist = WaterNetworkNode.builder().id(distId).name("Dist").code("D").type(WaterNetworkNodeType.DISTRIBUTION_POINT).build();
        WaterNetworkNode inl = WaterNetworkNode.builder().id(inletId).name("Inl").code("I").type(WaterNetworkNodeType.FIELD_INLET).fieldId(fieldId).build();

        UUID edge1Id = UUID.randomUUID();
        UUID edge2Id = UUID.randomUUID();

        WaterNetworkEdge e1 = WaterNetworkEdge.builder().id(edge1Id).name("E1").code("E1").type(WaterNetworkEdgeType.PRIMARY_CANAL).startNodeId(srcId).endNodeId(distId).maxCapacityM3S(new BigDecimal("5.0")).build();
        WaterNetworkEdge e2 = WaterNetworkEdge.builder().id(edge2Id).name("E2").code("E2").type(WaterNetworkEdgeType.SECONDARY_CANAL).startNodeId(distId).endNodeId(inletId).maxCapacityM3S(new BigDecimal("2.0")).build();

        ResilienceScenario scenario = evaluator.simulateScenario(
                "Canal Outage E2",
                "CANAL_OUTAGE",
                List.of(),
                List.of(edge2Id),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(src, dist, inl),
                List.of(e1, e2),
                Map.of(fieldId, new BigDecimal("600.00")),
                UUID.randomUUID()
        );

        assertThat(scenario.getAffectedFieldCount()).isEqualTo(1);
        assertThat(scenario.getLostCapacityM3S()).isEqualByComparingTo("2.0000");
        assertThat(scenario.getUnmetDemandM3()).isEqualByComparingTo("600.00");
        // Verify original edge was untouched
        assertThat(e2.getOperationalStatus()).isEqualTo(OperationalStatus.OPERATIONAL);
    }
}
