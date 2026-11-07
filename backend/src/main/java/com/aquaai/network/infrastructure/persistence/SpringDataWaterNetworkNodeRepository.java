package com.aquaai.network.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWaterNetworkNodeRepository extends JpaRepository<WaterNetworkNodeJpaEntity, UUID> {
    Optional<WaterNetworkNodeJpaEntity> findByCode(String code);
    Optional<WaterNetworkNodeJpaEntity> findByFieldId(UUID fieldId);
    List<WaterNetworkNodeJpaEntity> findByWuaId(UUID wuaId);
    List<WaterNetworkNodeJpaEntity> findByNodeType(String nodeType);
}
