package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.PilotProgram;
import com.aquaai.pilot.domain.model.PilotStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PilotProgramRepository {
    PilotProgram save(PilotProgram program);
    Optional<PilotProgram> findById(UUID id);
    Optional<PilotProgram> findByCode(String code);
    List<PilotProgram> findAll();
    List<PilotProgram> findByStatus(PilotStatus status);
    boolean existsByCode(String code);
}
