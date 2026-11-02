package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.GovernanceAuditLog;

import java.util.List;
import java.util.UUID;

public interface GovernanceAuditLogRepository {
    List<GovernanceAuditLog> findRecent(int limit);
    List<GovernanceAuditLog> findByProposalId(UUID proposalId);
    List<GovernanceAuditLog> findByVersionTag(String versionTag);
    GovernanceAuditLog save(GovernanceAuditLog log);
}
