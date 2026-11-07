package com.aquaai.wateraccounting.infrastructure.persistence.repository;

import com.aquaai.wateraccounting.domain.model.WaterLedgerEntry;
import com.aquaai.wateraccounting.domain.repository.WaterLedgerRepository;
import com.aquaai.wateraccounting.infrastructure.persistence.mapper.WaterLedgerEntryJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WaterLedgerRepositoryImpl implements WaterLedgerRepository {

    private final SpringDataWaterLedgerEntryRepository springDataRepository;
    private final WaterLedgerEntryJpaMapper mapper;

    @Override
    public WaterLedgerEntry save(WaterLedgerEntry entry) {
        var entity = mapper.toEntity(entry);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<WaterLedgerEntry> saveAll(List<WaterLedgerEntry> entries) {
        var entities = entries.stream().map(mapper::toEntity).collect(Collectors.toList());
        return springDataRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WaterLedgerEntry> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<WaterLedgerEntry> findAll() {
        return springDataRepository.findAllByOrderByEventTimestampDesc().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterLedgerEntry> findByFieldId(UUID fieldId) {
        return springDataRepository.findByFieldIdOrderByEventTimestampDesc(fieldId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterLedgerEntry> findByWuaId(UUID wuaId) {
        return springDataRepository.findByWuaIdOrderByEventTimestampDesc(wuaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterLedgerEntry> findByEdgeId(UUID edgeId) {
        return springDataRepository.findByEdgeIdOrderByEventTimestampDesc(edgeId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaterLedgerEntry> findByPeriod(Instant start, Instant end) {
        return springDataRepository.findByEventTimestampBetweenOrderByEventTimestampDesc(start, end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
