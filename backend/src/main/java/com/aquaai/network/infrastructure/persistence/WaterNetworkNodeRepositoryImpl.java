package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WaterNetworkNodeRepositoryImpl implements WaterNetworkNodeRepository {

    private final SpringDataWaterNetworkNodeRepository springDataRepository;
    private final WaterNetworkNodeJpaMapper mapper;

    @Override
    public WaterNetworkNode save(WaterNetworkNode node) {
        WaterNetworkNodeJpaEntity entity = mapper.toJpaEntity(node);
        WaterNetworkNodeJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WaterNetworkNode> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<WaterNetworkNode> findByCode(String code) {
        return springDataRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Optional<WaterNetworkNode> findByFieldId(UUID fieldId) {
        return springDataRepository.findByFieldId(fieldId).map(mapper::toDomain);
    }

    @Override
    public List<WaterNetworkNode> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterNetworkNode> findByWuaId(UUID wuaId) {
        return springDataRepository.findByWuaId(wuaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterNetworkNode> findByType(WaterNetworkNodeType type) {
        return springDataRepository.findByNodeType(type.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
