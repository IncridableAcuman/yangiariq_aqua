package com.aquaai.accountability.infrastructure.persistence.entity;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "water_incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterIncidentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "incident_number", nullable = false, unique = true, length = 32)
    private String incidentNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 64)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence", nullable = false, length = 32)
    private EvidenceConfidence confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private IncidentStatus status;

    @Column(name = "source_module", nullable = false, length = 64)
    private String sourceModule;

    @Column(name = "source_entity_type", nullable = false, length = 64)
    private String sourceEntityType;

    @Column(name = "source_entity_id")
    private UUID sourceEntityId;

    @Column(name = "finding_reference", length = 255)
    private String findingReference;

    @Column(name = "wua_id")
    private UUID wuaId;

    @Column(name = "canal_id")
    private UUID canalId;

    @Column(name = "field_id")
    private UUID fieldId;

    @Column(name = "location_description", length = 500)
    private String locationDescription;

    @Column(name = "detection_timestamp", nullable = false)
    private Instant detectionTimestamp;

    @Column(name = "sla_deadline")
    private Instant slaDeadline;

    @Column(name = "assigned_investigator_id")
    private UUID assignedInvestigatorId;

    @Column(name = "assigned_investigator_name", length = 255)
    private String assignedInvestigatorName;

    @Column(name = "responsible_organization", length = 255)
    private String responsibleOrganization;

    @Column(name = "root_cause_category", length = 128)
    private String rootCauseCategory;

    @Column(name = "root_cause_details", length = 2000)
    private String rootCauseDetails;

    @Column(name = "review_notes", length = 2000)
    private String reviewNotes;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "recurrence_count", nullable = false)
    private int recurrenceCount;

    @Column(name = "parent_incident_id")
    private UUID parentIncidentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
