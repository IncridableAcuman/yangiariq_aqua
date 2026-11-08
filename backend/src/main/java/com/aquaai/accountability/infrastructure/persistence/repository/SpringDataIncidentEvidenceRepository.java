package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.infrastructure.persistence.entity.IncidentEvidenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataIncidentEvidenceRepository extends JpaRepository<IncidentEvidenceJpaEntity, UUID> {
    List<IncidentEvidenceJpaEntity> findByIncidentIdOrderByRecordedAtAsc(UUID incidentId);
}
