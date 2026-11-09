package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.PilotFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPilotFieldRepository extends JpaRepository<PilotFieldEntity, UUID> {
    List<PilotFieldEntity> findByPilotId(UUID pilotId);
    Optional<PilotFieldEntity> findByPilotIdAndFieldId(UUID pilotId, UUID fieldId);
}
