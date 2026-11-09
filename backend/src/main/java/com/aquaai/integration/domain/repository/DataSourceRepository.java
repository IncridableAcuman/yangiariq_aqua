package com.aquaai.integration.domain.repository;

import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataSourceRepository {

    DataSource save(DataSource dataSource);

    Optional<DataSource> findById(UUID id);

    Optional<DataSource> findBySourceCode(String sourceCode);

    List<DataSource> findAll();

    List<DataSource> findAllByStatus(DataSourceStatus status);

    List<DataSource> findAllBySourceType(DataSourceType sourceType);

    void deleteById(UUID id);
}
