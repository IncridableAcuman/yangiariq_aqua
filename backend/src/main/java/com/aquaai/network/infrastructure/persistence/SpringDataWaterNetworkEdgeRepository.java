package com.aquaai.network.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWaterNetworkEdgeRepository extends JpaRepository<WaterNetworkEdgeJpaEntity, UUID> {
    Optional<WaterNetworkEdgeJpaEntity> findByCode(String code);
    List<WaterNetworkEdgeJpaEntity> findByStartNodeId(UUID startNodeId);
    List<WaterNetworkEdgeJpaEntity> findByEndNodeId(UUID endNodeId);
    List<WaterNetworkEdgeJpaEntity> findByEdgeType(String edgeType);
    List<WaterNetworkEdgeJpaEntity> findByWuaId(UUID wuaId);
}
