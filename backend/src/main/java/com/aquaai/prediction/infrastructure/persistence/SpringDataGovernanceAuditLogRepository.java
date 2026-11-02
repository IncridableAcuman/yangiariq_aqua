package com.aquaai.prediction.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataGovernanceAuditLogRepository extends JpaRepository<GovernanceAuditLogJpaEntity, UUID> {
    List<GovernanceAuditLogJpaEntity> findByProposalIdOrderByTimestampDesc(UUID proposalId);
    List<GovernanceAuditLogJpaEntity> findByVersionTagOrderByTimestampDesc(String versionTag);

    @Query("SELECT l FROM GovernanceAuditLogJpaEntity l ORDER BY l.timestamp DESC")
    List<GovernanceAuditLogJpaEntity> findRecentLogs(Pageable pageable);
}
