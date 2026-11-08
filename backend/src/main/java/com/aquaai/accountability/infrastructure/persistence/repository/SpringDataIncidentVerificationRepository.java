package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.infrastructure.persistence.entity.IncidentVerificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataIncidentVerificationRepository extends JpaRepository<IncidentVerificationJpaEntity, UUID> {
    List<IncidentVerificationJpaEntity> findByIncidentIdOrderByVerifiedAtDesc(UUID incidentId);
}
