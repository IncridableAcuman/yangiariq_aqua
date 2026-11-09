package com.aquaai.integration.infrastructure.persistence.repository;

import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import com.aquaai.integration.infrastructure.persistence.entity.DataSourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDataSourceRepository extends JpaRepository<DataSourceJpaEntity, UUID> {

    Optional<DataSourceJpaEntity> findBySourceCode(String sourceCode);

    List<DataSourceJpaEntity> findByStatus(DataSourceStatus status);

    List<DataSourceJpaEntity> findBySourceType(DataSourceType sourceType);
}
