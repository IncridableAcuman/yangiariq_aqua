package com.aquaai.problem.domain;

import com.aquaai.shared.domain.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "problem_reports")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReport extends BaseAuditableEntity {

    @Column(name = "reported_by", nullable = false)
    private UUID reportedBy;

    @Column(name = "field_id")
    private UUID fieldId;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    @Builder.Default
    private ProblemPriority priority = ProblemPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private ProblemStatus status = ProblemStatus.OPEN;

    @Column(name = "location_details", length = 255)
    private String locationDetails;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
}
