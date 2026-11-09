package com.aquaai.integration.infrastructure.persistence.repository;

import com.aquaai.integration.infrastructure.persistence.entity.ImportBatchJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataImportBatchRepository extends JpaRepository<ImportBatchJpaEntity, UUID> {

    List<ImportBatchJpaEntity> findAllByOrderByStartedAtDesc(Pageable pageable);
}
