package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.WaterUserAssociation;
import org.springframework.stereotype.Component;

@Component
public class WaterUserAssociationJpaMapper {

    public WaterUserAssociationJpaEntity toJpaEntity(WaterUserAssociation domain) {
        if (domain == null) return null;
        return WaterUserAssociationJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .code(domain.getCode())
                .district(domain.getDistrict())
                .contactPerson(domain.getContactPerson())
                .phoneNumber(domain.getPhoneNumber())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public WaterUserAssociation toDomain(WaterUserAssociationJpaEntity entity) {
        if (entity == null) return null;
        return WaterUserAssociation.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .district(entity.getDistrict())
                .contactPerson(entity.getContactPerson())
                .phoneNumber(entity.getPhoneNumber())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
