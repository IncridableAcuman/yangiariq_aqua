package com.aquaai.network.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.network.domain.model.OperationalStatus;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaterNetworkEdgeJpaMapper {

    private final ObjectMapper objectMapper;

    public WaterNetworkEdgeJpaEntity toJpaEntity(WaterNetworkEdge domain) {
        if (domain == null) return null;

        String geomJson = "[]";
        try {
            if (domain.getGeometry() != null) {
                geomJson = objectMapper.writeValueAsString(domain.getGeometry());
            }
        } catch (Exception e) {
            log.error("Failed to serialize edge geometry for id={}", domain.getId(), e);
        }

        return WaterNetworkEdgeJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .code(domain.getCode())
                .edgeType(domain.getType().name())
                .startNodeId(domain.getStartNodeId())
                .endNodeId(domain.getEndNodeId())
                .wuaId(domain.getWuaId())
                .lengthMeters(domain.getLengthMeters())
                .maxCapacityM3S(domain.getMaxCapacityM3S())
                .minFlowM3S(domain.getMinFlowM3S())
                .currentFlowM3S(domain.getCurrentFlowM3S())
                .operationalStatus(domain.getOperationalStatus().name())
                .geometryJson(geomJson)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public WaterNetworkEdge toDomain(WaterNetworkEdgeJpaEntity entity) {
        if (entity == null) return null;

        List<CoordinatePoint> geometry = Collections.emptyList();
        try {
            if (entity.getGeometryJson() != null && !entity.getGeometryJson().isBlank()) {
                geometry = objectMapper.readValue(entity.getGeometryJson(), new TypeReference<List<CoordinatePoint>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to deserialize edge geometry for id={}", entity.getId(), e);
        }

        WaterNetworkEdgeType type;
        try {
            type = WaterNetworkEdgeType.valueOf(entity.getEdgeType());
        } catch (Exception e) {
            type = WaterNetworkEdgeType.SECONDARY_CANAL;
        }

        OperationalStatus status;
        try {
            status = OperationalStatus.valueOf(entity.getOperationalStatus());
        } catch (Exception e) {
            status = OperationalStatus.OPERATIONAL;
        }

        return WaterNetworkEdge.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(type)
                .startNodeId(entity.getStartNodeId())
                .endNodeId(entity.getEndNodeId())
                .wuaId(entity.getWuaId())
                .lengthMeters(entity.getLengthMeters())
                .maxCapacityM3S(entity.getMaxCapacityM3S())
                .minFlowM3S(entity.getMinFlowM3S())
                .currentFlowM3S(entity.getCurrentFlowM3S())
                .operationalStatus(status)
                .geometry(geometry)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
