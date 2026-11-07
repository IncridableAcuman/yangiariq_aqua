package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaterNetworkNodeJpaMapper {

    private final ObjectMapper objectMapper;

    public WaterNetworkNodeJpaEntity toJpaEntity(WaterNetworkNode domain) {
        if (domain == null) return null;

        String propsJson = "{}";
        try {
            if (domain.getProperties() != null) {
                propsJson = objectMapper.writeValueAsString(domain.getProperties());
            }
        } catch (Exception e) {
            log.error("Failed to serialize node properties for id={}", domain.getId(), e);
        }

        return WaterNetworkNodeJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .code(domain.getCode())
                .nodeType(domain.getType().name())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .elevationMeters(domain.getElevationMeters())
                .waterSourceId(domain.getWaterSourceId())
                .fieldId(domain.getFieldId())
                .wuaId(domain.getWuaId())
                .isActive(domain.isActive())
                .propertiesJson(propsJson)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public WaterNetworkNode toDomain(WaterNetworkNodeJpaEntity entity) {
        if (entity == null) return null;

        Map<String, Object> props = Collections.emptyMap();
        try {
            if (entity.getPropertiesJson() != null && !entity.getPropertiesJson().isBlank()) {
                props = objectMapper.readValue(entity.getPropertiesJson(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to deserialize node properties for id={}", entity.getId(), e);
        }

        WaterNetworkNodeType type;
        try {
            type = WaterNetworkNodeType.valueOf(entity.getNodeType());
        } catch (Exception e) {
            type = WaterNetworkNodeType.CANAL_JUNCTION;
        }

        return WaterNetworkNode.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(type)
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .elevationMeters(entity.getElevationMeters())
                .waterSourceId(entity.getWaterSourceId())
                .fieldId(entity.getFieldId())
                .wuaId(entity.getWuaId())
                .isActive(entity.isActive())
                .properties(props)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
