package com.aquaai.network.application.service;

import com.aquaai.network.api.dto.ActualDeliveryResponse;
import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.api.dto.ResilienceScenarioResponse;
import com.aquaai.network.api.dto.WaterNetworkEdgeResponse;
import com.aquaai.network.api.dto.WaterNetworkNodeResponse;
import com.aquaai.network.domain.model.ActualDispatchDelivery;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.ResilienceScenario;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class WaterNetworkDtoMapper {

    public WaterNetworkNodeResponse toNodeResponse(WaterNetworkNode node) {
        if (node == null) return null;
        return new WaterNetworkNodeResponse(
                node.getId(),
                node.getName(),
                node.getCode(),
                node.getType().name(),
                node.getLatitude(),
                node.getLongitude(),
                node.getElevationMeters(),
                node.getWaterSourceId(),
                node.getFieldId(),
                node.getWuaId(),
                node.isActive(),
                node.getProperties()
        );
    }

    public WaterNetworkEdgeResponse toEdgeResponse(WaterNetworkEdge edge) {
        if (edge == null) return null;
        BigDecimal util = BigDecimal.ZERO;
        if (edge.getMaxCapacityM3S() != null && edge.getMaxCapacityM3S().compareTo(BigDecimal.ZERO) > 0) {
            util = edge.getCurrentFlowM3S().multiply(new BigDecimal("100.00"))
                    .divide(edge.getMaxCapacityM3S(), 2, RoundingMode.HALF_UP);
        }

        return new WaterNetworkEdgeResponse(
                edge.getId(),
                edge.getName(),
                edge.getCode(),
                edge.getType().name(),
                edge.getStartNodeId(),
                edge.getEndNodeId(),
                edge.getWuaId(),
                edge.getLengthMeters(),
                edge.getMaxCapacityM3S(),
                edge.getMinFlowM3S(),
                edge.getCurrentFlowM3S(),
                edge.getOperationalStatus().name(),
                util,
                edge.getGeometry()
        );
    }

    public DispatchScheduleResponse toScheduleResponse(DispatchSchedule s) {
        if (s == null) return null;
        return new DispatchScheduleResponse(
                s.getId(),
                s.getTitle(),
                s.getPeriodStart(),
                s.getPeriodEnd(),
                s.getStatus().name(),
                s.getTargetWuaId(),
                s.getTargetWuaName(),
                s.getTotalRequestedM3(),
                s.getTotalAllocatedM3(),
                s.getTotalUnmetM3(),
                s.getAssignments(),
                s.getConflicts(),
                s.getMetrics(),
                s.getFairnessPolicy(),
                s.getNotes(),
                s.getApprovedBy(),
                s.getApprovedAt(),
                s.getRejectionReason(),
                s.getCreatedBy(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    public ActualDeliveryResponse toDeliveryResponse(ActualDispatchDelivery d) {
        if (d == null) return null;
        return new ActualDeliveryResponse(
                d.getId(),
                d.getDispatchScheduleId(),
                d.getFieldId(),
                d.getFieldName(),
                d.getEdgeId(),
                d.getCanalName(),
                d.getActualStartAt(),
                d.getActualEndAt(),
                d.getPlannedVolumeM3(),
                d.getActualVolumeM3(),
                d.getVarianceVolumeM3(),
                d.getVariancePercent(),
                d.getMeasuredFlowM3S(),
                d.getOperatorId(),
                d.getOperatorNotes(),
                d.getCreatedAt()
        );
    }

    public ResilienceScenarioResponse toScenarioResponse(ResilienceScenario s) {
        if (s == null) return null;
        return new ResilienceScenarioResponse(
                s.getId(),
                s.getName(),
                s.getScenarioType(),
                s.getDisabledNodeIds(),
                s.getDisabledEdgeIds(),
                s.getCapacityReductionPercent(),
                s.getDemandSurgePercent(),
                s.getAffectedFieldCount(),
                s.getLostCapacityM3S(),
                s.getUnmetDemandM3(),
                s.getAffectedWuaNames(),
                s.getAlternativeFeasibleRoutesCount(),
                s.getSummary(),
                s.getSimulationResults(),
                s.getCreatedBy(),
                s.getCreatedAt()
        );
    }
}
