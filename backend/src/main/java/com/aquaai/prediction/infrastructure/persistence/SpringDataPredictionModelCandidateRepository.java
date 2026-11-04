package com.aquaai.prediction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPredictionModelCandidateRepository extends JpaRepository<PredictionModelCandidateJpaEntity, UUID> {

    Optional<PredictionModelCandidateJpaEntity> findByVersionTag(String versionTag);

    List<PredictionModelCandidateJpaEntity> findByPredictionTypeOrderByCreatedAtDesc(String predictionType);

    Optional<PredictionModelCandidateJpaEntity> findFirstByPredictionTypeAndStatusOrderByCreatedAtDesc(String predictionType, String status);
}
