package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.NetworkTopologyResponse;
import com.aquaai.network.api.dto.WaterNetworkEdgeResponse;
import com.aquaai.network.api.dto.WaterNetworkNodeResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.service.NetworkTopologyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetNetworkTopologyUseCase {

    private final WaterNetworkNodeRepository nodeRepository;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final NetworkTopologyValidator topologyValidator;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public NetworkTopologyResponse execute() {
        List<WaterNetworkNode> nodes = nodeRepository.findAll();
        List<WaterNetworkEdge> edges = edgeRepository.findAll();

        NetworkTopologyValidator.TopologyValidationResult validationResult = topologyValidator.validate(nodes, edges);

        List<WaterNetworkNodeResponse> nodeResponses = nodes.stream()
                .map(dtoMapper::toNodeResponse)
                .collect(Collectors.toList());

        List<WaterNetworkEdgeResponse> edgeResponses = edges.stream()
                .map(dtoMapper::toEdgeResponse)
                .collect(Collectors.toList());

        return new NetworkTopologyResponse(
                nodeResponses,
                edgeResponses,
                validationResult.errors(),
                validationResult.warnings(),
                validationResult.isValid()
        );
    }
}
