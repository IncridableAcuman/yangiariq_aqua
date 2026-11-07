package com.aquaai.wateraccounting.domain.repository;

import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterReconciliationRepository {
    WaterReconciliationItem save(WaterReconciliationItem item);
    List<WaterReconciliationItem> saveAll(List<WaterReconciliationItem> items);
    Optional<WaterReconciliationItem> findById(UUID id);
    List<WaterReconciliationItem> findAll();
    List<WaterReconciliationItem> findByBoundaryType(String boundaryType);
    long count();
}
