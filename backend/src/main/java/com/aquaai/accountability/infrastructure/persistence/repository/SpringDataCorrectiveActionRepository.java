package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.infrastructure.persistence.entity.CorrectiveActionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataCorrectiveActionRepository extends JpaRepository<CorrectiveActionJpaEntity, UUID> {
    List<CorrectiveActionJpaEntity> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
