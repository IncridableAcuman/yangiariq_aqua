package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.BacktestRun;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BacktestRunRepository {
    Optional<BacktestRun> findById(UUID id);
    List<BacktestRun> findByProposalId(UUID proposalId);
    Optional<BacktestRun> findLatestByProposalId(UUID proposalId);
    BacktestRun save(BacktestRun run);
}
