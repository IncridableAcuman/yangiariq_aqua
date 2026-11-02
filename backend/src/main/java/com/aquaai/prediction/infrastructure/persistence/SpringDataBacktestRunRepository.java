package com.aquaai.prediction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataBacktestRunRepository extends JpaRepository<BacktestRunJpaEntity, UUID> {
    List<BacktestRunJpaEntity> findByProposalIdOrderByExecutedAtDesc(UUID proposalId);
    Optional<BacktestRunJpaEntity> findFirstByProposalIdOrderByExecutedAtDesc(UUID proposalId);
}
