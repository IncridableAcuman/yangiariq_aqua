package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GovernanceAuditLogRepositoryImpl implements GovernanceAuditLogRepository {

    private final SpringDataGovernanceAuditLogRepository springDataRepository;
    private final PredictionGovernanceJpaMapper mapper;

    @Override
    public List<GovernanceAuditLog> findRecent(int limit) {
        return springDataRepository.findRecentLogs(PageRequest.of(0, Math.max(1, limit))).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GovernanceAuditLog> findByProposalId(UUID proposalId) {
        return springDataRepository.findByProposalIdOrderByTimestampDesc(proposalId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GovernanceAuditLog> findByVersionTag(String versionTag) {
        return springDataRepository.findByVersionTagOrderByTimestampDesc(versionTag).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public GovernanceAuditLog save(GovernanceAuditLog log) {
        GovernanceAuditLogJpaEntity entity = mapper.toEntity(log);
        GovernanceAuditLogJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
