package com.aquaai.integration.infrastructure.persistence;

import com.aquaai.integration.domain.model.ImportBatch;
import com.aquaai.integration.domain.model.ImportRowResult;
import com.aquaai.integration.domain.repository.ImportBatchRepository;
import com.aquaai.integration.infrastructure.persistence.entity.ImportBatchJpaEntity;
import com.aquaai.integration.infrastructure.persistence.entity.ImportRowResultJpaEntity;
import com.aquaai.integration.infrastructure.persistence.mapper.ImportBatchJpaMapper;
import com.aquaai.integration.infrastructure.persistence.repository.SpringDataImportBatchRepository;
import com.aquaai.integration.infrastructure.persistence.repository.SpringDataImportRowResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ImportBatchRepositoryImpl implements ImportBatchRepository {

    private final SpringDataImportBatchRepository batchRepository;
    private final SpringDataImportRowResultRepository rowResultRepository;

    @Override
    public ImportBatch save(ImportBatch batch) {
        ImportBatchJpaEntity entity = ImportBatchJpaMapper.toEntity(batch);
        ImportBatchJpaEntity saved = batchRepository.save(entity);
        return ImportBatchJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<ImportBatch> findById(UUID id) {
        return batchRepository.findById(id).map(ImportBatchJpaMapper::toDomain);
    }

    @Override
    public List<ImportBatch> findAll(int offset, int limit) {
        int page = offset / Math.max(1, limit);
        return batchRepository.findAllByOrderByStartedAtDesc(PageRequest.of(page, Math.min(limit, 100)))
                .stream().map(ImportBatchJpaMapper::toDomain).toList();
    }

    @Override
    public void saveRowResults(List<ImportRowResult> results) {
        if (results == null || results.isEmpty()) return;
        List<ImportRowResultJpaEntity> entities = results.stream().map(ImportBatchJpaMapper::toEntity).toList();
        rowResultRepository.saveAll(entities);
    }

    @Override
    public List<ImportRowResult> findRowResultsByBatchId(UUID batchId, int offset, int limit) {
        int page = offset / Math.max(1, limit);
        return rowResultRepository.findByBatchIdOrderByRowNumberAsc(batchId, PageRequest.of(page, Math.min(limit, 100)))
                .stream().map(ImportBatchJpaMapper::toDomain).toList();
    }
}
