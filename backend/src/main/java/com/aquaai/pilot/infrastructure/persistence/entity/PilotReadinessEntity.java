package com.aquaai.pilot.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pilot_readiness_assessments")
public class PilotReadinessEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "pilot_id", nullable = false)
    private UUID pilotId;

    @Column(name = "overall_status", nullable = false, length = 32)
    private String overallStatus;

    @Column(name = "infrastructure_status", nullable = false, length = 32)
    private String infrastructureStatus;

    @Column(name = "data_status", nullable = false, length = 32)
    private String dataStatus;

    @Column(name = "operational_status", nullable = false, length = 32)
    private String operationalStatus;

    @Column(name = "governance_status", nullable = false, length = 32)
    private String governanceStatus;

    @Column(name = "checks_json", nullable = false, columnDefinition = "TEXT")
    private String checksJson;

    @Column(name = "assessed_by", nullable = false)
    private UUID assessedBy;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    public PilotReadinessEntity() {}

    public PilotReadinessEntity(
            UUID id,
            UUID pilotId,
            String overallStatus,
            String infrastructureStatus,
            String dataStatus,
            String operationalStatus,
            String governanceStatus,
            String checksJson,
            UUID assessedBy,
            Instant assessedAt) {
        this.id = id;
        this.pilotId = pilotId;
        this.overallStatus = overallStatus;
        this.infrastructureStatus = infrastructureStatus;
        this.dataStatus = dataStatus;
        this.operationalStatus = operationalStatus;
        this.governanceStatus = governanceStatus;
        this.checksJson = checksJson;
        this.assessedBy = assessedBy;
        this.assessedAt = assessedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPilotId() { return pilotId; }
    public void setPilotId(UUID pilotId) { this.pilotId = pilotId; }
    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    public String getInfrastructureStatus() { return infrastructureStatus; }
    public void setInfrastructureStatus(String infrastructureStatus) { this.infrastructureStatus = infrastructureStatus; }
    public String getDataStatus() { return dataStatus; }
    public void setDataStatus(String dataStatus) { this.dataStatus = dataStatus; }
    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }
    public String getGovernanceStatus() { return governanceStatus; }
    public void setGovernanceStatus(String governanceStatus) { this.governanceStatus = governanceStatus; }
    public String getChecksJson() { return checksJson; }
    public void setChecksJson(String checksJson) { this.checksJson = checksJson; }
    public UUID getAssessedBy() { return assessedBy; }
    public void setAssessedBy(UUID assessedBy) { this.assessedBy = assessedBy; }
    public Instant getAssessedAt() { return assessedAt; }
    public void setAssessedAt(Instant assessedAt) { this.assessedAt = assessedAt; }
}
