package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.PilotReadinessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPilotReadinessRepository extends JpaRepository<PilotReadinessEntity, UUID> {
    List<PilotReadinessEntity> findByPilotIdOrderByAssessedAtDesc(UUID pilotId);
    Optional<PilotReadinessEntity> findFirstByPilotIdOrderByAssessedAtDesc(UUID pilotId);
}
