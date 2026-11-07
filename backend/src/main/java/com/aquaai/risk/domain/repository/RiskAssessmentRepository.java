package com.aquaai.risk.domain.repository;

import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskSeverity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for persisting and retrieving farm risk assessments.
 */
public interface RiskAssessmentRepository {

    FarmRiskAssessment save(FarmRiskAssessment assessment);

    Optional<FarmRiskAssessment> findById(UUID id);

    Optional<FarmRiskAssessment> findLatestByFieldId(UUID fieldId);

    List<FarmRiskAssessment> findAllLatestByFieldIds(Collection<UUID> fieldIds);

    List<FarmRiskAssessment> findByFieldIdHistory(UUID fieldId, Instant from, Instant to, int page, int size);

    long countHistoricalByFieldId(UUID fieldId, Instant from, Instant to);

    long countBySeverity(RiskSeverity severity, Collection<UUID> fieldIds);

    long countByLifecycleStatus(RiskAssessmentLifecycleStatus status, Collection<UUID> fieldIds);
}
