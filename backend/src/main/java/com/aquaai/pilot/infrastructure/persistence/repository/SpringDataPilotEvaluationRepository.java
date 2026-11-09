package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.PilotEvaluationSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPilotEvaluationRepository extends JpaRepository<PilotEvaluationSnapshotEntity, UUID> {
    List<PilotEvaluationSnapshotEntity> findByPilotIdOrderByCreatedAtDesc(UUID pilotId);
    Optional<PilotEvaluationSnapshotEntity> findFirstByPilotIdOrderByCreatedAtDesc(UUID pilotId);
}
