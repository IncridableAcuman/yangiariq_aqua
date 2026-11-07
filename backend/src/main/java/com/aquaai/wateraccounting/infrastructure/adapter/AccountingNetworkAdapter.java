package com.aquaai.wateraccounting.infrastructure.adapter;

import com.aquaai.network.domain.model.ActualDispatchDelivery;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.repository.ActualDispatchDeliveryRepository;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import com.aquaai.wateraccounting.domain.port.AccountingNetworkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountingNetworkAdapter implements AccountingNetworkPort {

    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterNetworkNodeRepository nodeRepository;
    private final ActualDispatchDeliveryRepository deliveryRepository;
    private final WaterUserAssociationRepository wuaRepository;

    @Override
    public List<CanalEdgeSummary> getAllCanalEdges() {
        Map<UUID, String> nodeNames = nodeRepository.findAll().stream()
                .collect(Collectors.toMap(WaterNetworkNode::getId, WaterNetworkNode::getName, (a, b) -> a));

        return edgeRepository.findAll().stream()
                .map(edge -> new CanalEdgeSummary(
                        edge.getId(),
                        edge.getName(),
                        edge.getCode(),
                        edge.getType() != null ? edge.getType().name() : "PRIMARY_CANAL",
                        edge.getStartNodeId(),
                        nodeNames.getOrDefault(edge.getStartNodeId(), "Start Node"),
                        edge.getEndNodeId(),
                        nodeNames.getOrDefault(edge.getEndNodeId(), "End Node"),
                        edge.getMaxCapacityM3S(),
                        edge.getCurrentFlowM3S(),
                        edge.getLengthMeters()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliverySummary> getAllActualDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(this::mapDelivery)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliverySummary> getDeliveriesForField(UUID fieldId) {
        return deliveryRepository.findByFieldId(fieldId).stream()
                .map(this::mapDelivery)
                .collect(Collectors.toList());
    }

    @Override
    public List<WuaSummary> getAllWuas() {
        return wuaRepository.findAll().stream()
                .map(w -> new WuaSummary(w.getId(), w.getName(), w.getCode(), w.getDistrict()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WuaSummary> getWuaById(UUID wuaId) {
        return wuaRepository.findById(wuaId)
                .map(w -> new WuaSummary(w.getId(), w.getName(), w.getCode(), w.getDistrict()));
    }

    @Override
    public Map<UUID, UUID> getFieldToWuaMapping() {
        Map<UUID, UUID> mapping = new HashMap<>();
        List<WaterNetworkNode> nodes = nodeRepository.findAll();
        for (WaterNetworkNode node : nodes) {
            if (node.getFieldId() != null && node.getWuaId() != null) {
                mapping.put(node.getFieldId(), node.getWuaId());
            }
        }
        return mapping;
    }

    private DeliverySummary mapDelivery(ActualDispatchDelivery d) {
        return new DeliverySummary(
                d.getId(),
                d.getDispatchScheduleId(),
                d.getFieldId(),
                d.getFieldName(),
                d.getEdgeId(),
                d.getCanalName(),
                d.getPlannedVolumeM3(),
                d.getActualVolumeM3(),
                d.getMeasuredFlowM3S(),
                d.getActualStartAt(),
                d.getActualEndAt()
        );
    }
}
