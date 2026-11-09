package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.PilotProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPilotProgramRepository extends JpaRepository<PilotProgramEntity, UUID> {
    Optional<PilotProgramEntity> findByCode(String code);
    List<PilotProgramEntity> findByStatus(String status);
    boolean existsByCode(String code);
}
