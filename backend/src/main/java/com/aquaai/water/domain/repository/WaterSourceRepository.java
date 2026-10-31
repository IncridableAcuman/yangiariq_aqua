package com.aquaai.water.domain.repository;

import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.model.WaterSourcePage;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for {@link WaterSource} persistence.
 * Decoupled from ORM and Spring Data infrastructure.
 */
public interface WaterSourceRepository {

    WaterSource save(WaterSource waterSource);

    Optional<WaterSource> findById(UUID id);

    Optional<WaterSource> findByNameIgnoreCaseAndStatus(String name, WaterSourceStatus status);

    List<WaterSource> findAll();

    List<WaterSource> findByStatus(WaterSourceStatus status);

    List<WaterSource> findByType(WaterSourceType type);

    boolean existsById(UUID id);

    boolean existsByNameIgnoreCaseAndStatus(String name, WaterSourceStatus status);

    long countByStatus(WaterSourceStatus status);

    WaterSourcePage findWithFilters(
            String search,
            WaterSourceType type,
            WaterSourceStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    void deleteById(UUID id);
}
