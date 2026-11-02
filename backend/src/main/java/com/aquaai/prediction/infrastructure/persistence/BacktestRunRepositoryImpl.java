package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.BacktestRun;
import com.aquaai.prediction.domain.port.BacktestRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BacktestRunRepositoryImpl implements BacktestRunRepository {

    private final SpringDataBacktestRunRepository springDataRepository;
    private final PredictionGovernanceJpaMapper mapper;

    @Override
    public Optional<BacktestRun> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<BacktestRun> findByProposalId(UUID proposalId) {
        return springDataRepository.findByProposalIdOrderByExecutedAtDesc(proposalId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BacktestRun> findLatestByProposalId(UUID proposalId) {
        return springDataRepository.findFirstByProposalIdOrderByExecutedAtDesc(proposalId)
                .map(mapper::toDomain);
    }

    @Override
    public BacktestRun save(BacktestRun run) {
        BacktestRunJpaEntity entity = mapper.toEntity(run);
        BacktestRunJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
