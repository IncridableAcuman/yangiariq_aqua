package com.aquaai.accountability.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for regional operational water incidents, investigation lifecycle, and corrective actions.
 * Enforces separation of duties, deterministic transitions, and non-accusatory governance.
 */
@Getter
@Builder(toBuilder = true)
public class WaterIncident {

    private final UUID id;
    private final String incidentNumber;
    private String title;
    private String description;
    private IncidentCategory category;
    private IncidentSeverity severity;
    private EvidenceConfidence confidence;
    private IncidentStatus status;
    private final String sourceModule;
    private final String sourceEntityType;
    private final UUID sourceEntityId;
    private final String findingReference;
    private UUID wuaId;
    private UUID canalId;
    private UUID fieldId;
    private String locationDescription;
    private final Instant detectionTimestamp;
    private Instant slaDeadline;
    private UUID assignedInvestigatorId;
    private String assignedInvestigatorName;
    private String responsibleOrganization;
    private String rootCauseCategory;
    private String rootCauseDetails;
    private String reviewNotes;
    private UUID reviewedBy;
    private Instant reviewedAt;
    private String resolutionNotes;
    private int recurrenceCount;
    private UUID parentIncidentId;
    private final Instant createdAt;
    private Instant updatedAt;

    public WaterIncident(
            UUID id,
            String incidentNumber,
            String title,
            String description,
            IncidentCategory category,
            IncidentSeverity severity,
            EvidenceConfidence confidence,
            IncidentStatus status,
            String sourceModule,
            String sourceEntityType,
            UUID sourceEntityId,
            String findingReference,
            UUID wuaId,
            UUID canalId,
            UUID fieldId,
            String locationDescription,
            Instant detectionTimestamp,
            Instant slaDeadline,
            UUID assignedInvestigatorId,
            String assignedInvestigatorName,
            String responsibleOrganization,
            String rootCauseCategory,
            String rootCauseDetails,
            String reviewNotes,
            UUID reviewedBy,
            Instant reviewedAt,
            String resolutionNotes,
            int recurrenceCount,
            UUID parentIncidentId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.incidentNumber = incidentNumber != null ? incidentNumber : generateIncidentNumber();
        this.title = Objects.requireNonNull(title, "Title cannot be null").trim();
        this.description = Objects.requireNonNull(description, "Description cannot be null").trim();
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.confidence = confidence != null ? confidence : EvidenceConfidence.UNVERIFIED;
        this.status = status != null ? status : IncidentStatus.DETECTED;
        this.sourceModule = Objects.requireNonNull(sourceModule, "Source module cannot be null");
        this.sourceEntityType = Objects.requireNonNull(sourceEntityType, "Source entity type cannot be null");
        this.sourceEntityId = sourceEntityId;
        this.findingReference = findingReference;
        this.wuaId = wuaId;
        this.canalId = canalId;
        this.fieldId = fieldId;
        this.locationDescription = locationDescription;
        this.detectionTimestamp = detectionTimestamp != null ? detectionTimestamp : Instant.now();
        this.slaDeadline = slaDeadline;
        this.assignedInvestigatorId = assignedInvestigatorId;
        this.assignedInvestigatorName = assignedInvestigatorName;
        this.responsibleOrganization = responsibleOrganization;
        this.rootCauseCategory = rootCauseCategory;
        this.rootCauseDetails = rootCauseDetails;
        this.reviewNotes = reviewNotes;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.resolutionNotes = resolutionNotes;
        this.recurrenceCount = Math.max(0, recurrenceCount);
        this.parentIncidentId = parentIncidentId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    private static String generateIncidentNumber() {
        return "INC-" + (System.currentTimeMillis() % 1000000);
    }

    public boolean isOverdue(Instant now) {
        if (this.status.isTerminal()) {
            return false;
        }
        return slaDeadline != null && now.isAfter(slaDeadline);
    }

    public void updateSlaDeadline(Instant newDeadline) {
        this.slaDeadline = newDeadline;
        this.updatedAt = Instant.now();
    }

    public void review(UUID reviewerId, boolean accept, String notes) {
        if (this.status != IncidentStatus.DETECTED && this.status != IncidentStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only DETECTED or UNDER_REVIEW incidents can be reviewed. Current: " + this.status);
        }
        this.reviewedBy = Objects.requireNonNull(reviewerId, "Reviewer ID cannot be null");
        this.reviewedAt = Instant.now();
        this.reviewNotes = Objects.requireNonNull(notes, "Review notes are required").trim();
        this.status = accept ? IncidentStatus.OPEN : IncidentStatus.DISMISSED;
        this.updatedAt = Instant.now();
    }

    public void startReview() {
        if (this.status != IncidentStatus.DETECTED) {
            throw new IllegalStateException("Only DETECTED incidents can transition to UNDER_REVIEW");
        }
        this.status = IncidentStatus.UNDER_REVIEW;
        this.updatedAt = Instant.now();
    }

    public void assignInvestigator(UUID investigatorId, String investigatorName, String organization) {
        if (!this.status.allowsInvestigation()) {
            throw new IllegalStateException("Cannot assign investigator in state: " + this.status);
        }
        this.assignedInvestigatorId = Objects.requireNonNull(investigatorId, "Investigator ID cannot be null");
        this.assignedInvestigatorName = investigatorName;
        this.responsibleOrganization = organization;
        if (this.status == IncidentStatus.OPEN) {
            this.status = IncidentStatus.INVESTIGATING;
        }
        this.updatedAt = Instant.now();
    }

    public void recordInvestigation(String rootCauseCategory, String rootCauseDetails, EvidenceConfidence updatedConfidence) {
        if (!this.status.allowsInvestigation()) {
            throw new IllegalStateException("Cannot record investigation findings in state: " + this.status);
        }
        this.rootCauseCategory = rootCauseCategory;
        this.rootCauseDetails = rootCauseDetails;
        if (updatedConfidence != null) {
            // Cannot upgrade simulated to verified
            if (this.confidence == EvidenceConfidence.SIMULATED && updatedConfidence == EvidenceConfidence.VERIFIED) {
                throw new IllegalArgumentException("Cannot directly convert SIMULATED evidence to VERIFIED without physical proof");
            }
            this.confidence = updatedConfidence;
        }
        if (this.status == IncidentStatus.OPEN) {
            this.status = IncidentStatus.INVESTIGATING;
        }
        this.updatedAt = Instant.now();
    }

    public void transitionTo(IncidentStatus targetStatus, String reason) {
        this.status = Objects.requireNonNull(targetStatus, "Target status cannot be null");
        this.updatedAt = Instant.now();
    }

    public void resolve(String resolutionNotes) {
        this.status = IncidentStatus.RESOLVED;
        this.resolutionNotes = Objects.requireNonNull(resolutionNotes, "Resolution notes required").trim();
        this.updatedAt = Instant.now();
    }

    public void dismiss(UUID reviewerId, String reason) {
        this.reviewedBy = reviewerId;
        this.reviewedAt = Instant.now();
        this.reviewNotes = Objects.requireNonNull(reason, "Dismissal reason required").trim();
        this.status = IncidentStatus.DISMISSED;
        this.updatedAt = Instant.now();
    }

    public void reopen(String reason) {
        if (this.status != IncidentStatus.RESOLVED && this.status != IncidentStatus.DISMISSED && this.status != IncidentStatus.CLOSED) {
            throw new IllegalStateException("Only terminal incidents can be reopened. Current status: " + this.status);
        }
        this.status = IncidentStatus.REOPENED;
        this.recurrenceCount++;
        this.resolutionNotes = (this.resolutionNotes != null ? this.resolutionNotes + "\n" : "") +
                "[REOPENED] " + Objects.requireNonNull(reason, "Reopen reason required").trim();
        this.updatedAt = Instant.now();
    }

    public void close(String reason) {
        if (this.status != IncidentStatus.RESOLVED) {
            throw new IllegalStateException("Only RESOLVED incidents can be CLOSED. Current: " + this.status);
        }
        this.status = IncidentStatus.CLOSED;
        this.updatedAt = Instant.now();
    }
}
