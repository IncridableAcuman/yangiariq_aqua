package com.aquaai.network.domain.repository;

import com.aquaai.network.domain.model.ResilienceScenario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResilienceScenarioRepository {
    ResilienceScenario save(ResilienceScenario scenario);
    Optional<ResilienceScenario> findById(UUID id);
    List<ResilienceScenario> findAll();
    long count();
}
