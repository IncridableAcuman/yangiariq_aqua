package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PilotReadinessAssessment {
    private final UUID id;
    private final UUID pilotId;
    private final ReadinessResult overallStatus;
    private final ReadinessResult infrastructureStatus;
    private final ReadinessResult dataStatus;
    private final ReadinessResult operationalStatus;
    private final ReadinessResult governanceStatus;
    private final List<PilotReadinessCheck> checks;
    private final UUID assessedBy;
    private final Instant assessedAt;

    public PilotReadinessAssessment(
            UUID id,
            UUID pilotId,
            ReadinessResult overallStatus,
            ReadinessResult infrastructureStatus,
            ReadinessResult dataStatus,
            ReadinessResult operationalStatus,
            ReadinessResult governanceStatus,
            List<PilotReadinessCheck> checks,
            UUID assessedBy,
            Instant assessedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.pilotId = Objects.requireNonNull(pilotId, "pilotId must not be null");
        this.overallStatus = Objects.requireNonNull(overallStatus, "overallStatus must not be null");
        this.infrastructureStatus = Objects.requireNonNull(infrastructureStatus, "infrastructureStatus must not be null");
        this.dataStatus = Objects.requireNonNull(dataStatus, "dataStatus must not be null");
        this.operationalStatus = Objects.requireNonNull(operationalStatus, "operationalStatus must not be null");
        this.governanceStatus = Objects.requireNonNull(governanceStatus, "governanceStatus must not be null");
        this.checks = checks != null ? List.copyOf(checks) : Collections.emptyList();
        this.assessedBy = Objects.requireNonNull(assessedBy, "assessedBy must not be null");
        this.assessedAt = assessedAt != null ? assessedAt : Instant.now();
    }

    public static PilotReadinessAssessment evaluate(
            UUID pilotId,
            UUID assessedBy,
            List<PilotReadinessCheck> checks) {
        ReadinessResult infra = evaluateCategory(checks, ReadinessCategory.INFRASTRUCTURE);
        ReadinessResult data = evaluateCategory(checks, ReadinessCategory.DATA);
        ReadinessResult ops = evaluateCategory(checks, ReadinessCategory.OPERATIONAL);
        ReadinessResult gov = evaluateCategory(checks, ReadinessCategory.GOVERNANCE);

        boolean hasMandatoryBlocked = checks.stream()
                .anyMatch(c -> c.isMandatory() && c.getResult() == ReadinessResult.BLOCKED);

        ReadinessResult overall;
        if (hasMandatoryBlocked) {
            overall = ReadinessResult.BLOCKED;
        } else if (checks.stream().anyMatch(c -> c.getResult() == ReadinessResult.WARNING)) {
            overall = ReadinessResult.WARNING;
        } else if (checks.stream().allMatch(c -> c.getResult() == ReadinessResult.PASS)) {
            overall = ReadinessResult.PASS;
        } else {
            overall = ReadinessResult.NOT_ASSESSED;
        }

        return new PilotReadinessAssessment(
                UUID.randomUUID(),
                pilotId,
                overall,
                infra,
                data,
                ops,
                gov,
                checks,
                assessedBy,
                Instant.now()
        );
    }

    private static ReadinessResult evaluateCategory(List<PilotReadinessCheck> checks, ReadinessCategory category) {
        List<PilotReadinessCheck> catChecks = checks.stream()
                .filter(c -> c.getCategory() == category)
                .toList();
        if (catChecks.isEmpty()) {
            return ReadinessResult.NOT_ASSESSED;
        }
        if (catChecks.stream().anyMatch(c -> c.isMandatory() && c.getResult() == ReadinessResult.BLOCKED)) {
            return ReadinessResult.BLOCKED;
        }
        if (catChecks.stream().anyMatch(c -> c.getResult() == ReadinessResult.WARNING)) {
            return ReadinessResult.WARNING;
        }
        if (catChecks.stream().allMatch(c -> c.getResult() == ReadinessResult.PASS)) {
            return ReadinessResult.PASS;
        }
        return ReadinessResult.NOT_ASSESSED;
    }

    public UUID getId() { return id; }
    public UUID getPilotId() { return pilotId; }
    public ReadinessResult getOverallStatus() { return overallStatus; }
    public ReadinessResult getInfrastructureStatus() { return infrastructureStatus; }
    public ReadinessResult getDataStatus() { return dataStatus; }
    public ReadinessResult getOperationalStatus() { return operationalStatus; }
    public ReadinessResult getGovernanceStatus() { return governanceStatus; }
    public List<PilotReadinessCheck> getChecks() { return checks; }
    public UUID getAssessedBy() { return assessedBy; }
    public Instant getAssessedAt() { return assessedAt; }
}
