package com.aquaai.prediction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCalibrationProposalRepository extends JpaRepository<CalibrationProposalJpaEntity, UUID> {
    Optional<CalibrationProposalJpaEntity> findByProposalCode(String proposalCode);
    List<CalibrationProposalJpaEntity> findByPredictionType(String predictionType);

    @Query("SELECT p FROM CalibrationProposalJpaEntity p WHERE p.status IN ('DRAFT', 'BACKTESTING', 'REVIEW_REQUIRED', 'APPROVED') ORDER BY p.createdAt DESC")
    List<CalibrationProposalJpaEntity> findOpenProposals();
}
