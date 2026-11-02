package com.aquaai.prediction.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Measurable, evidence-backed calibration proposal generated when model performance degrades
 * or systematic bias is detected.
 * <p>
 * CRITICAL SAFETY: System NEVER modifies parameters autonomously. Human review is mandatory.
 */
public class CalibrationProposal {

    private final UUID id;
    private final String proposalCode;
    private final PredictionType predictionType;
    private final String currentVersionTag;
    private final String candidateVersionTag;
    private ProposalStatus status;
    private final String reason;
    private final String evidence;
    private final String candidateChanges;
    private final String proposedConfigurationJson;
    private final String expectedEffect;
    private final GovernanceRiskLevel riskLevel;
    private final Instant createdAt;
    private Instant updatedAt;
    private final String createdBy;
    private String reviewedBy;
    private Instant reviewedAt;
    private String reviewNotes;
    private Long optLockVersion;

    public CalibrationProposal(
            UUID id,
            String proposalCode,
            PredictionType predictionType,
            String currentVersionTag,
            String candidateVersionTag,
            ProposalStatus status,
            String reason,
            String evidence,
            String candidateChanges,
            String proposedConfigurationJson,
            String expectedEffect,
            GovernanceRiskLevel riskLevel,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String reviewedBy,
            Instant reviewedAt,
            String reviewNotes,
            Long optLockVersion
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.proposalCode = Objects.requireNonNull(proposalCode, "proposalCode must not be null");
        this.predictionType = Objects.requireNonNull(predictionType, "predictionType must not be null");
        this.currentVersionTag = Objects.requireNonNull(currentVersionTag, "currentVersionTag must not be null");
        this.candidateVersionTag = Objects.requireNonNull(candidateVersionTag, "candidateVersionTag must not be null");
        this.status = status != null ? status : ProposalStatus.DRAFT;
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.evidence = Objects.requireNonNull(evidence, "evidence must not be null");
        this.candidateChanges = Objects.requireNonNull(candidateChanges, "candidateChanges must not be null");
        this.proposedConfigurationJson = Objects.requireNonNull(proposedConfigurationJson, "proposedConfigurationJson must not be null");
        this.expectedEffect = expectedEffect != null ? expectedEffect : "";
        this.riskLevel = riskLevel != null ? riskLevel : GovernanceRiskLevel.MEDIUM;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.createdBy = createdBy != null ? createdBy : "SYSTEM_DIAGNOSTICS";
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNotes = reviewNotes;
        this.optLockVersion = optLockVersion != null ? optLockVersion : 0L;
    }

    public static CalibrationProposal create(
            String proposalCode,
            PredictionType predictionType,
            String currentVersionTag,
            String candidateVersionTag,
            String reason,
            String evidence,
            String candidateChanges,
            String proposedConfigurationJson,
            String expectedEffect,
            GovernanceRiskLevel riskLevel,
            String createdBy
    ) {
        return new CalibrationProposal(
                UUID.randomUUID(),
                proposalCode,
                predictionType,
                currentVersionTag,
                candidateVersionTag,
                ProposalStatus.DRAFT,
                reason,
                evidence,
                candidateChanges,
                proposedConfigurationJson,
                expectedEffect,
                riskLevel,
                Instant.now(),
                Instant.now(),
                createdBy,
                null,
                null,
                null,
                0L
        );
    }

    public void startBacktest() {
        if (this.status != ProposalStatus.DRAFT && this.status != ProposalStatus.REVIEW_REQUIRED) {
            throw new IllegalStateException("Cannot start backtest from status: " + this.status);
        }
        this.status = ProposalStatus.BACKTESTING;
        this.updatedAt = Instant.now();
    }

    public void completeBacktest() {
        this.status = ProposalStatus.REVIEW_REQUIRED;
        this.updatedAt = Instant.now();
    }

    public void approve(String reviewer, String notes) {
        if (this.status != ProposalStatus.REVIEW_REQUIRED) {
            throw new IllegalStateException("Proposal cannot be approved without completed backtest review. Current status: " + this.status);
        }
        this.status = ProposalStatus.APPROVED;
        this.reviewedBy = Objects.requireNonNull(reviewer, "reviewer must not be null");
        this.reviewedAt = Instant.now();
        this.reviewNotes = notes != null ? notes : "Approved for controlled rollout.";
        this.updatedAt = Instant.now();
    }

    public void reject(String reviewer, String notes) {
        if (this.status == ProposalStatus.ACTIVE || this.status == ProposalStatus.ROLLED_BACK) {
            throw new IllegalStateException("Cannot reject an active or rolled-back proposal.");
        }
        this.status = ProposalStatus.REJECTED;
        this.reviewedBy = Objects.requireNonNull(reviewer, "reviewer must not be null");
        this.reviewedAt = Instant.now();
        this.reviewNotes = notes != null ? notes : "Rejected by human operator.";
        this.updatedAt = Instant.now();
    }

    public void markActive() {
        if (this.status != ProposalStatus.APPROVED) {
            throw new IllegalStateException("Only approved proposals can be activated. Current status: " + this.status);
        }
        this.status = ProposalStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void markRolledBack(String notes) {
        this.status = ProposalStatus.ROLLED_BACK;
        this.reviewNotes = (this.reviewNotes != null ? this.reviewNotes + " | " : "") + "Rolled back: " + notes;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getProposalCode() { return proposalCode; }
    public PredictionType getPredictionType() { return predictionType; }
    public String getCurrentVersionTag() { return currentVersionTag; }
    public String getCandidateVersionTag() { return candidateVersionTag; }
    public ProposalStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public String getEvidence() { return evidence; }
    public String getCandidateChanges() { return candidateChanges; }
    public String getProposedConfigurationJson() { return proposedConfigurationJson; }
    public String getExpectedEffect() { return expectedEffect; }
    public GovernanceRiskLevel getRiskLevel() { return riskLevel; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getReviewedBy() { return reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getReviewNotes() { return reviewNotes; }
    public Long getOptLockVersion() { return optLockVersion; }
}
