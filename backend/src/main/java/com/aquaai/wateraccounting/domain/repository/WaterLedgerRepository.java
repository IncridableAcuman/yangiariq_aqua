package com.aquaai.wateraccounting.domain.repository;

import com.aquaai.wateraccounting.domain.model.WaterLedgerEntry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterLedgerRepository {
    WaterLedgerEntry save(WaterLedgerEntry entry);
    List<WaterLedgerEntry> saveAll(List<WaterLedgerEntry> entries);
    Optional<WaterLedgerEntry> findById(UUID id);
    List<WaterLedgerEntry> findAll();
    List<WaterLedgerEntry> findByFieldId(UUID fieldId);
    List<WaterLedgerEntry> findByWuaId(UUID wuaId);
    List<WaterLedgerEntry> findByEdgeId(UUID edgeId);
    List<WaterLedgerEntry> findByPeriod(Instant start, Instant end);
    long count();
}
