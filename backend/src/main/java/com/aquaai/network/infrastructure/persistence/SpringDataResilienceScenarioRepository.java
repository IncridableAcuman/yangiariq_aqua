package com.aquaai.network.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataResilienceScenarioRepository extends JpaRepository<ResilienceScenarioJpaEntity, UUID> {
}
