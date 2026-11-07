package com.aquaai.wateraccounting.infrastructure.persistence.repository;

import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
import com.aquaai.wateraccounting.infrastructure.persistence.mapper.WaterReconciliationJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WaterReconciliationRepositoryImpl implements WaterReconciliationRepository {

    private final SpringDataWaterReconciliationRepository springDataRepository;
    private final WaterReconciliationJpaMapper mapper;

    @Override
    public WaterReconciliationItem save(WaterReconciliationItem item) {
        var entity = mapper.toEntity(item);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<WaterReconciliationItem> saveAll(List<WaterReconciliationItem> items) {
        var entities = items.stream().map(mapper::toEntity).collect(Collectors.toList());
        return springDataRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WaterReconciliationItem> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<WaterReconciliationItem> findAll() {
        return springDataRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterReconciliationItem> findByBoundaryType(String boundaryType) {
        return springDataRepository.findByBoundaryTypeOrderByPeriodStartDesc(boundaryType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
