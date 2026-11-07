package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.SegmentCapacityResponse;
import com.aquaai.network.domain.model.NetworkCapacity;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.port.NetworkAnomalyPort;
import com.aquaai.network.domain.port.NetworkFieldPort;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.service.NetworkCapacityEngine;
import com.aquaai.network.domain.service.PathResolutionEngine;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSegmentCapacityUseCase {

    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterNetworkNodeRepository nodeRepository;
    private final NetworkCapacityEngine capacityEngine;
    private final PathResolutionEngine pathResolutionEngine;
    private final NetworkAnomalyPort anomalyPort;
    private final NetworkFieldPort fieldPort;

    @Transactional(readOnly = true)
    public SegmentCapacityResponse execute(UUID edgeId) {
        WaterNetworkEdge edge = edgeRepository.findById(edgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal segment", edgeId));

        List<WaterNetworkNode> allNodes = nodeRepository.findAll();
        List<WaterNetworkEdge> allEdges = edgeRepository.findAll();

        List<UUID> downstreamFieldIds = pathResolutionEngine.findDownstreamFieldIds(edgeId, allNodes, allEdges);

        boolean hydraulicInterlock = false;
        String interlockReason = null;
        List<String> violations = new ArrayList<>();

        for (UUID fieldId : downstreamFieldIds) {
            if (anomalyPort.hasSuspectedPipeBurst(fieldId)) {
                hydraulicInterlock = true;
                interlockReason = "Downstream field has active suspected pipe burst";
                violations.add(interlockReason + " (Field ID: " + fieldId + ")");
            }
            if (anomalyPort.hasCriticalPressureDrop(fieldId)) {
                hydraulicInterlock = true;
                interlockReason = "Downstream field has critical pressure drop";
                violations.add(interlockReason + " (Field ID: " + fieldId + ")");
            }
        }

        NetworkCapacity capacity = capacityEngine.evaluateSegmentCapacity(
                edge,
                BigDecimal.ZERO,
                hydraulicInterlock,
                interlockReason
        );

        List<String> dependentFieldNames = downstreamFieldIds.stream()
                .map(id -> fieldPort.getFieldInfo(id).map(NetworkFieldPort.FieldParcelInfo::name).orElse(id.toString()))
                .toList();

        return new SegmentCapacityResponse(
                edge.getId(),
                edge.getName(),
                edge.getCode(),
                edge.getType().name(),
                capacity.maxCapacityM3S(),
                capacity.currentFlowM3S(),
                capacity.remainingCapacityM3S(),
                capacity.utilizationPercent(),
                capacity.constraintStatus(),
                hydraulicInterlock,
                interlockReason,
                violations,
                downstreamFieldIds.size(),
                dependentFieldNames
        );
    }
}
