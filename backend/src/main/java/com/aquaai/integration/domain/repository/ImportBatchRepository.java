package com.aquaai.integration.domain.repository;

import com.aquaai.integration.domain.model.ImportBatch;
import com.aquaai.integration.domain.model.ImportRowResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportBatchRepository {

    ImportBatch save(ImportBatch batch);

    Optional<ImportBatch> findById(UUID id);

    List<ImportBatch> findAll(int offset, int limit);

    void saveRowResults(List<ImportRowResult> results);

    List<ImportRowResult> findRowResultsByBatchId(UUID batchId, int offset, int limit);
}
