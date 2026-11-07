package com.aquaai.network.domain.repository;

import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterNetworkEdgeRepository {
    WaterNetworkEdge save(WaterNetworkEdge edge);
    Optional<WaterNetworkEdge> findById(UUID id);
    Optional<WaterNetworkEdge> findByCode(String code);
    List<WaterNetworkEdge> findAll();
    List<WaterNetworkEdge> findByStartNodeId(UUID startNodeId);
    List<WaterNetworkEdge> findByEndNodeId(UUID endNodeId);
    List<WaterNetworkEdge> findByType(WaterNetworkEdgeType type);
    List<WaterNetworkEdge> findByWuaId(UUID wuaId);
    long count();
}
