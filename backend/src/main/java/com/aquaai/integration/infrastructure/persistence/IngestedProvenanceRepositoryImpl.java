package com.aquaai.integration.infrastructure.persistence;

import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.domain.model.IngestedProvenance;
import com.aquaai.integration.domain.repository.IngestedProvenanceRepository;
import com.aquaai.integration.infrastructure.persistence.entity.IngestedProvenanceJpaEntity;
import com.aquaai.integration.infrastructure.persistence.mapper.IngestedProvenanceJpaMapper;
import com.aquaai.integration.infrastructure.persistence.repository.SpringDataIngestedProvenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IngestedProvenanceRepositoryImpl implements IngestedProvenanceRepository {

    private final SpringDataIngestedProvenanceRepository springDataRepository;

    @Override
    public IngestedProvenance save(IngestedProvenance provenance) {
        IngestedProvenanceJpaEntity entity = IngestedProvenanceJpaMapper.toEntity(provenance);
        IngestedProvenanceJpaEntity saved = springDataRepository.save(entity);
        return IngestedProvenanceJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<IngestedProvenance> findById(UUID id) {
        return springDataRepository.findById(id).map(IngestedProvenanceJpaMapper::toDomain);
    }

    @Override
    public Optional<IngestedProvenance> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey).map(IngestedProvenanceJpaMapper::toDomain);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public List<IngestedProvenance> findRecent(int limit) {
        return springDataRepository.findAllByOrderByIngestedAtDesc(PageRequest.of(0, Math.min(limit, 100)))
                .stream().map(IngestedProvenanceJpaMapper::toDomain).toList();
    }

    @Override
    public List<IngestedProvenance> findRecentByDataSourceId(UUID dataSourceId, int limit) {
        return springDataRepository.findByDataSourceIdOrderByIngestedAtDesc(dataSourceId, PageRequest.of(0, Math.min(limit, 100)))
                .stream().map(IngestedProvenanceJpaMapper::toDomain).toList();
    }

    @Override
    public List<IngestedProvenance> findRecentByFieldId(UUID fieldId, int limit) {
        return springDataRepository.findByFieldIdOrderByMeasuredAtDesc(fieldId, PageRequest.of(0, Math.min(limit, 100)))
                .stream().map(IngestedProvenanceJpaMapper::toDomain).toList();
    }

    @Override
    public Map<DataQualityStatus, Long> getQualityMetricsSummary(Instant since) {
        Map<DataQualityStatus, Long> map = new EnumMap<>(DataQualityStatus.class);
        for (DataQualityStatus status : DataQualityStatus.values()) {
            map.put(status, 0L);
        }
        Instant threshold = since != null ? since : Instant.EPOCH;
        List<Object[]> rows = springDataRepository.countByQualityStatusSince(threshold);
        for (Object[] row : rows) {
            DataQualityStatus status = (DataQualityStatus) row[0];
            Long count = (Long) row[1];
            map.put(status, count);
        }
        return map;
    }

    @Override
    public long countTotalIngested() {
        return springDataRepository.count();
    }

    @Override
    public long countByQualityStatus(DataQualityStatus status) {
        return springDataRepository.countByQualityStatus(status);
    }
}
