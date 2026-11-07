package com.aquaai.network.domain.repository;

import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterNetworkNodeRepository {
    WaterNetworkNode save(WaterNetworkNode node);
    Optional<WaterNetworkNode> findById(UUID id);
    Optional<WaterNetworkNode> findByCode(String code);
    Optional<WaterNetworkNode> findByFieldId(UUID fieldId);
    List<WaterNetworkNode> findAll();
    List<WaterNetworkNode> findByWuaId(UUID wuaId);
    List<WaterNetworkNode> findByType(WaterNetworkNodeType type);
    long count();
}
