package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.PilotParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPilotParticipantRepository extends JpaRepository<PilotParticipantEntity, UUID> {
    List<PilotParticipantEntity> findByPilotId(UUID pilotId);
    List<PilotParticipantEntity> findByUserId(UUID userId);
    Optional<PilotParticipantEntity> findByPilotIdAndUserId(UUID pilotId, UUID userId);
}
