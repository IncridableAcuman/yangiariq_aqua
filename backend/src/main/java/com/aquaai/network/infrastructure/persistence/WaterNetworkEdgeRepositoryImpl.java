package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WaterNetworkEdgeRepositoryImpl implements WaterNetworkEdgeRepository {

    private final SpringDataWaterNetworkEdgeRepository springDataRepository;
    private final WaterNetworkEdgeJpaMapper mapper;

    @Override
    public WaterNetworkEdge save(WaterNetworkEdge edge) {
        WaterNetworkEdgeJpaEntity entity = mapper.toJpaEntity(edge);
        WaterNetworkEdgeJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WaterNetworkEdge> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<WaterNetworkEdge> findByCode(String code) {
        return springDataRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<WaterNetworkEdge> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterNetworkEdge> findByStartNodeId(UUID startNodeId) {
        return springDataRepository.findByStartNodeId(startNodeId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterNetworkEdge> findByEndNodeId(UUID endNodeId) {
        return springDataRepository.findByEndNodeId(endNodeId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterNetworkEdge> findByType(WaterNetworkEdgeType type) {
        return springDataRepository.findByEdgeType(type.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterNetworkEdge> findByWuaId(UUID wuaId) {
        return springDataRepository.findByWuaId(wuaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
