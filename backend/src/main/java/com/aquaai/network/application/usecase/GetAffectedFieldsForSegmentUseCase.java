package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.AffectedFieldsResponse;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.port.NetworkFieldPort;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.service.PathResolutionEngine;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAffectedFieldsForSegmentUseCase {

    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterNetworkNodeRepository nodeRepository;
    private final PathResolutionEngine pathResolutionEngine;
    private final NetworkFieldPort fieldPort;

    @Transactional(readOnly = true)
    public AffectedFieldsResponse execute(UUID edgeId) {
        WaterNetworkEdge edge = edgeRepository.findById(edgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal segment", edgeId));

        List<WaterNetworkNode> allNodes = nodeRepository.findAll();
        List<WaterNetworkEdge> allEdges = edgeRepository.findAll();

        List<UUID> downstreamFieldIds = pathResolutionEngine.findDownstreamFieldIds(edgeId, allNodes, allEdges);

        List<AffectedFieldsResponse.AffectedFieldItem> items = new ArrayList<>();
        for (UUID fieldId : downstreamFieldIds) {
            fieldPort.getFieldInfo(fieldId).ifPresent(info -> items.add(
                    new AffectedFieldsResponse.AffectedFieldItem(
                            info.id(),
                            info.name(),
                            info.cropType(),
                            info.areaHectares(),
                            "Owner (" + info.ownerId() + ")"
                    )
            ));
        }

        return new AffectedFieldsResponse(
                edge.getId(),
                edge.getName(),
                items.size(),
                items
        );
    }
}
