package com.aquaai.wateraccounting.infrastructure.persistence.repository;

import com.aquaai.wateraccounting.infrastructure.persistence.entity.WaterLedgerEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWaterLedgerEntryRepository extends JpaRepository<WaterLedgerEntryJpaEntity, UUID> {
    List<WaterLedgerEntryJpaEntity> findByFieldIdOrderByEventTimestampDesc(UUID fieldId);
    List<WaterLedgerEntryJpaEntity> findByWuaIdOrderByEventTimestampDesc(UUID wuaId);
    List<WaterLedgerEntryJpaEntity> findByEdgeIdOrderByEventTimestampDesc(UUID edgeId);
    List<WaterLedgerEntryJpaEntity> findByEventTimestampBetweenOrderByEventTimestampDesc(Instant start, Instant end);
    List<WaterLedgerEntryJpaEntity> findAllByOrderByEventTimestampDesc();
}
