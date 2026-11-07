package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WaterUserAssociationRepositoryImpl implements WaterUserAssociationRepository {

    private final SpringDataWaterUserAssociationRepository springDataRepository;
    private final WaterUserAssociationJpaMapper mapper;

    @Override
    public WaterUserAssociation save(WaterUserAssociation wua) {
        WaterUserAssociationJpaEntity entity = mapper.toJpaEntity(wua);
        WaterUserAssociationJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WaterUserAssociation> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<WaterUserAssociation> findByCode(String code) {
        return springDataRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<WaterUserAssociation> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
