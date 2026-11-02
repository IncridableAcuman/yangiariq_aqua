package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.BacktestRun;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.GovernanceRiskLevel;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.ProposalStatus;
import org.springframework.stereotype.Component;

@Component
public class PredictionGovernanceJpaMapper {

    public PredictionMethodVersion toDomain(PredictionMethodVersionJpaEntity e) {
        if (e == null) return null;
        return new PredictionMethodVersion(
                e.getId(),
                PredictionType.valueOf(e.getPredictionType()),
                e.getVersionTag(),
                MethodVersionStatus.valueOf(e.getStatus()),
                e.getDescription(),
                e.getConfigurationJson(),
                e.isImmutable(),
                e.getCreatedAt(),
                e.getActivatedAt(),
                e.getRetiredAt(),
                e.getCreatedBy(),
                e.getActivatedBy(),
                e.getOptLockVersion()
        );
    }

    public PredictionMethodVersionJpaEntity toEntity(PredictionMethodVersion d) {
        if (d == null) return null;
        return PredictionMethodVersionJpaEntity.builder()
                .id(d.getId())
                .predictionType(d.getPredictionType().name())
                .versionTag(d.getVersionTag())
                .status(d.getStatus().name())
                .description(d.getDescription())
                .configurationJson(d.getConfigurationJson())
                .isImmutable(d.isImmutable())
                .createdAt(d.getCreatedAt())
                .activatedAt(d.getActivatedAt())
                .retiredAt(d.getRetiredAt())
                .createdBy(d.getCreatedBy())
                .activatedBy(d.getActivatedBy())
                .optLockVersion(d.getOptLockVersion())
                .build();
    }

    public CalibrationProposal toDomain(CalibrationProposalJpaEntity e) {
        if (e == null) return null;
        return new CalibrationProposal(
                e.getId(),
                e.getProposalCode(),
                PredictionType.valueOf(e.getPredictionType()),
                e.getCurrentVersionTag(),
                e.getCandidateVersionTag(),
                ProposalStatus.valueOf(e.getStatus()),
                e.getReason(),
                e.getEvidence(),
                e.getCandidateChanges(),
                e.getProposedConfigurationJson(),
                e.getExpectedEffect(),
                GovernanceRiskLevel.valueOf(e.getRiskLevel()),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getCreatedBy(),
                e.getReviewedBy(),
                e.getReviewedAt(),
                e.getReviewNotes(),
                e.getOptLockVersion()
        );
    }

    public CalibrationProposalJpaEntity toEntity(CalibrationProposal d) {
        if (d == null) return null;
        return CalibrationProposalJpaEntity.builder()
                .id(d.getId())
                .proposalCode(d.getProposalCode())
                .predictionType(d.getPredictionType().name())
                .currentVersionTag(d.getCurrentVersionTag())
                .candidateVersionTag(d.getCandidateVersionTag())
                .status(d.getStatus().name())
                .reason(d.getReason())
                .evidence(d.getEvidence())
                .candidateChanges(d.getCandidateChanges())
                .proposedConfigurationJson(d.getProposedConfigurationJson())
                .expectedEffect(d.getExpectedEffect())
                .riskLevel(d.getRiskLevel().name())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .createdBy(d.getCreatedBy())
                .reviewedBy(d.getReviewedBy())
                .reviewedAt(d.getReviewedAt())
                .reviewNotes(d.getReviewNotes())
                .optLockVersion(d.getOptLockVersion())
                .build();
    }

    public BacktestRun toDomain(BacktestRunJpaEntity e) {
        if (e == null) return null;
        return new BacktestRun(
                e.getId(),
                e.getProposalId(),
                e.getCurrentVersionTag(),
                e.getCandidateVersionTag(),
                e.getDateRangeFrom(),
                e.getDateRangeTo(),
                e.getSampleSize(),
                e.getCurrentMae(),
                e.getCandidateMae(),
                e.getMaeImprovementPercent(),
                e.getCurrentRmse(),
                e.getCandidateRmse(),
                e.getCurrentMbe(),
                e.getCandidateMbe(),
                e.getCurrentAccuracyPercent(),
                e.getCandidateAccuracyPercent(),
                e.getCurrentF1(),
                e.getCandidateF1(),
                e.isRegressionDetected(),
                e.getRegressionDetails(),
                EvaluationDataQuality.valueOf(e.getDataQuality()),
                e.isComparable(),
                e.isHasSufficientSample(),
                e.getExecutedAt(),
                e.getExecutedBy()
        );
    }

    public BacktestRunJpaEntity toEntity(BacktestRun d) {
        if (d == null) return null;
        return BacktestRunJpaEntity.builder()
                .id(d.getId())
                .proposalId(d.getProposalId())
                .currentVersionTag(d.getCurrentVersionTag())
                .candidateVersionTag(d.getCandidateVersionTag())
                .dateRangeFrom(d.getDateRangeFrom())
                .dateRangeTo(d.getDateRangeTo())
                .sampleSize(d.getSampleSize())
                .currentMae(d.getCurrentMae())
                .candidateMae(d.getCandidateMae())
                .maeImprovementPercent(d.getMaeImprovementPercent())
                .currentRmse(d.getCurrentRmse())
                .candidateRmse(d.getCandidateRmse())
                .currentMbe(d.getCurrentMbe())
                .candidateMbe(d.getCandidateMbe())
                .currentAccuracyPercent(d.getCurrentAccuracyPercent())
                .candidateAccuracyPercent(d.getCandidateAccuracyPercent())
                .currentF1(d.getCurrentF1())
                .candidateF1(d.getCandidateF1())
                .regressionDetected(d.isRegressionDetected())
                .regressionDetails(d.getRegressionDetails())
                .dataQuality(d.getDataQuality().name())
                .isComparable(d.isComparable())
                .hasSufficientSample(d.isHasSufficientSample())
                .executedAt(d.getExecutedAt())
                .executedBy(d.getExecutedBy())
                .build();
    }

    public GovernanceAuditLog toDomain(GovernanceAuditLogJpaEntity e) {
        if (e == null) return null;
        return new GovernanceAuditLog(
                e.getId(),
                GovernanceAction.valueOf(e.getAction()),
                PredictionType.valueOf(e.getPredictionType()),
                e.getVersionTag(),
                e.getProposalId(),
                e.getActorId(),
                e.getActorRole(),
                e.getTimestamp(),
                e.getReason(),
                e.getPreviousState(),
                e.getNewState(),
                e.getDetailsJson()
        );
    }

    public GovernanceAuditLogJpaEntity toEntity(GovernanceAuditLog d) {
        if (d == null) return null;
        return GovernanceAuditLogJpaEntity.builder()
                .id(d.getId())
                .action(d.getAction().name())
                .predictionType(d.getPredictionType().name())
                .versionTag(d.getVersionTag())
                .proposalId(d.getProposalId())
                .actorId(d.getActorId())
                .actorRole(d.getActorRole())
                .timestamp(d.getTimestamp())
                .reason(d.getReason())
                .previousState(d.getPreviousState())
                .newState(d.getNewState())
                .detailsJson(d.getDetailsJson())
                .build();
    }
}
