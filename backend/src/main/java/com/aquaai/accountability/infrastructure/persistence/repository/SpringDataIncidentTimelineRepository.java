package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.infrastructure.persistence.entity.IncidentTimelineJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataIncidentTimelineRepository extends JpaRepository<IncidentTimelineJpaEntity, UUID> {
    List<IncidentTimelineJpaEntity> findByIncidentIdOrderByTimestampAsc(UUID incidentId);
}
