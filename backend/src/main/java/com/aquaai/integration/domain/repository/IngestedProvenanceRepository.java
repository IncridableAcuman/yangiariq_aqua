package com.aquaai.integration.domain.repository;

import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.domain.model.IngestedProvenance;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IngestedProvenanceRepository {

    IngestedProvenance save(IngestedProvenance provenance);

    Optional<IngestedProvenance> findById(UUID id);

    Optional<IngestedProvenance> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<IngestedProvenance> findRecent(int limit);

    List<IngestedProvenance> findRecentByDataSourceId(UUID dataSourceId, int limit);

    List<IngestedProvenance> findRecentByFieldId(UUID fieldId, int limit);

    Map<DataQualityStatus, Long> getQualityMetricsSummary(Instant since);

    long countTotalIngested();

    long countByQualityStatus(DataQualityStatus status);
}
