package com.aquaai.prediction.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calibration_proposals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalibrationProposalJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "proposal_code", nullable = false, unique = true, length = 64)
    private String proposalCode;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "current_version_tag", nullable = false, length = 64)
    private String currentVersionTag;

    @Column(name = "candidate_version_tag", nullable = false, length = 64)
    private String candidateVersionTag;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "evidence", nullable = false, columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "candidate_changes", nullable = false, columnDefinition = "TEXT")
    private String candidateChanges;

    @Column(name = "proposed_configuration_json", nullable = false, columnDefinition = "TEXT")
    private String proposedConfigurationJson;

    @Column(name = "expected_effect", columnDefinition = "TEXT")
    private String expectedEffect;

    @Column(name = "risk_level", nullable = false, length = 32)
    private String riskLevel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "reviewed_by", length = 64)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Version
    @Column(name = "opt_lock_version", nullable = false)
    private Long optLockVersion;
}
