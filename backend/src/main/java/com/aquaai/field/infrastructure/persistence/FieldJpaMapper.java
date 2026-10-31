package com.aquaai.field.infrastructure.persistence;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.shared.domain.BusinessRuleException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Maps between pure domain {@link Field} aggregates and {@link FieldJpaEntity} persistence records.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FieldJpaMapper {

    private final ObjectMapper objectMapper;

    /**
     * Converts a JPA entity to a pure domain Field aggregate.
     */
    public Field toDomain(FieldJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        FieldLocation location = null;
        if (entity.getLatitude() != null && entity.getLongitude() != null) {
            location = FieldLocation.of(entity.getLatitude(), entity.getLongitude());
        }

        FieldBoundary boundary = deserializeBoundary(entity.getBoundary());

        return new Field(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAreaHectares(),
                entity.getCropType(),
                entity.getSoilType(),
                entity.getIrrigationMethod(),
                location,
                boundary,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    /**
     * Converts a domain Field aggregate to a JPA entity.
     */
    public FieldJpaEntity toEntity(Field domain) {
        if (domain == null) {
            return null;
        }

        FieldJpaEntity entity = new FieldJpaEntity();
        // If domain has a version, it is an existing persisted entity; preserve its assigned ID and version.
        // If domain has no version, it is a new entity; let Hibernate generate the UUID identifier.
        if (domain.getVersion() != null) {
            entity.setId(domain.getId());
            entity.setVersion(domain.getVersion());
        }
        entity.setOwnerId(domain.getOwnerId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setAreaHectares(domain.getAreaHectares());
        entity.setCropType(domain.getCropType());
        entity.setSoilType(domain.getSoilType());
        entity.setIrrigationMethod(domain.getIrrigationMethod());

        if (domain.getLocation() != null) {
            entity.setLatitude(domain.getLocation().latitude());
            entity.setLongitude(domain.getLocation().longitude());
        } else {
            entity.setLatitude(null);
            entity.setLongitude(null);
        }

        entity.setBoundary(serializeBoundary(domain.getBoundary()));
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }

    /**
     * Updates an existing JPA entity from a domain Field aggregate.
     */
    public void updateEntityFromDomain(Field domain, FieldJpaEntity entity) {
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setAreaHectares(domain.getAreaHectares());
        entity.setCropType(domain.getCropType());
        entity.setSoilType(domain.getSoilType());
        entity.setIrrigationMethod(domain.getIrrigationMethod());

        if (domain.getLocation() != null) {
            entity.setLatitude(domain.getLocation().latitude());
            entity.setLongitude(domain.getLocation().longitude());
        } else {
            entity.setLatitude(null);
            entity.setLongitude(null);
        }

        entity.setBoundary(serializeBoundary(domain.getBoundary()));
        entity.setStatus(domain.getStatus());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }

    private String serializeBoundary(FieldBoundary boundary) {
        if (boundary == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(boundary);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize field boundary to JSON", e);
            throw new BusinessRuleException("Failed to serialize field boundary: " + e.getMessage());
        }
    }

    private FieldBoundary deserializeBoundary(String boundaryJson) {
        if (boundaryJson == null || boundaryJson.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(boundaryJson);
            if (node.isTextual()) {
                return objectMapper.readValue(node.asText(), FieldBoundary.class);
            }
            return objectMapper.treeToValue(node, FieldBoundary.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize field boundary from JSON: {}", boundaryJson, e);
            throw new BusinessRuleException("Invalid field boundary JSON format stored in database");
        }
    }
}
