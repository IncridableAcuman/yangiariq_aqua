package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PromoteCandidateRequest;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.port.PredictionModelCandidateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Use Case governing state promotion of ML model candidates.
 * Enforces human review: candidates never automatically promote or self-activate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromoteModelCandidateUseCase {

    private final PredictionModelCandidateRepository candidateRepository;
    private final PredictionMethodVersionRepository versionRepository;
    private final GovernanceAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public PredictionModelCandidate execute(UUID candidateId, PromoteCandidateRequest request, String actor, String role) {
        PredictionModelCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Model candidate not found: " + candidateId));

        MethodVersionStatus targetStatus = request.targetStatus();
        MethodVersionStatus prevStatus = candidate.getStatus();

        if (targetStatus == prevStatus) {
            return candidate; // Idempotent
        }

        // Regression Guard Check: cannot activate or canary if regression was detected
        if (targetStatus == MethodVersionStatus.ACTIVE || targetStatus == MethodVersionStatus.CANARY) {
            boolean regression = checkRegressionDetected(candidate);
            if (regression) {
                throw new IllegalStateException("Promotion blocked by Regression Guard: Candidate demonstrates performance regression or unacceptable bias drift.");
            }
        }

        Instant now = Instant.now();

        // If activating, deprecate any currently active versions of this type
        if (targetStatus == MethodVersionStatus.ACTIVE) {
            Optional<PredictionMethodVersion> currentActive = versionRepository.findActiveByPredictionType(candidate.getPredictionType());
            currentActive.ifPresent(v -> {
                v.deprecate(actor);
                versionRepository.save(v);
            });

            Optional<PredictionModelCandidate> currentActiveCand = candidateRepository.findActiveByPredictionType(candidate.getPredictionType());
            currentActiveCand.ifPresent(c -> {
                c.rollback(actor);
                candidateRepository.save(c);
            });

            candidate.activate(actor);
        } else if (targetStatus == MethodVersionStatus.APPROVED) {
            candidate.approve(actor);
        } else if (targetStatus == MethodVersionStatus.SHADOW) {
            candidate.setShadow(actor);
        } else if (targetStatus == MethodVersionStatus.CANARY) {
            candidate.setCanary(actor);
        } else if (targetStatus == MethodVersionStatus.RETIRED) {
            candidate.retire(actor);
        }

        PredictionModelCandidate savedCandidate = candidateRepository.save(candidate);

        // Synchronize with PredictionMethodVersion
        Optional<PredictionMethodVersion> versionOpt = versionRepository.findByVersionTag(candidate.getVersionTag());
        if (versionOpt.isPresent()) {
            PredictionMethodVersion ver = versionOpt.get();
            if (targetStatus == MethodVersionStatus.ACTIVE) {
                ver.activate(actor);
            } else if (targetStatus == MethodVersionStatus.APPROVED) {
                ver.approve(actor);
            } else if (targetStatus == MethodVersionStatus.SHADOW) {
                ver.setShadow(actor);
            } else if (targetStatus == MethodVersionStatus.CANARY) {
                ver.setCanary(actor);
            } else if (targetStatus == MethodVersionStatus.RETIRED) {
                ver.retire(actor);
            }
            versionRepository.save(ver);
        }

        // Governance Audit Log
        GovernanceAction action = switch (targetStatus) {
            case APPROVED -> GovernanceAction.PROPOSAL_APPROVED;
            case SHADOW -> GovernanceAction.VERSION_SHADOW_STARTED;
            case CANARY -> GovernanceAction.VERSION_CANARY_STARTED;
            case ACTIVE -> GovernanceAction.VERSION_ACTIVATED;
            default -> GovernanceAction.VERSION_ROLLBACK;
        };

        auditLogRepository.save(new GovernanceAuditLog(
                UUID.randomUUID(),
                action,
                candidate.getPredictionType(),
                candidate.getVersionTag(),
                null,
                actor != null ? actor : "ADMIN",
                role != null ? role : "ADMIN",
                now,
                request.reason() != null ? request.reason() : "Governed promotion to " + targetStatus,
                prevStatus.name(),
                targetStatus.name(),
                String.format("{\"candidateId\":\"%s\",\"artifactHash\":\"%s\"}", candidate.getId(), candidate.getArtifactHash())
        ));

        log.info("Model candidate {} promoted from {} to {} by {}", candidate.getVersionTag(), prevStatus, targetStatus, actor);
        return savedCandidate;
    }

    private boolean checkRegressionDetected(PredictionModelCandidate c) {
        try {
            if (c.getBaselineComparisonMetricsJson() != null && !c.getBaselineComparisonMetricsJson().isBlank()) {
                JsonNode root = objectMapper.readTree(c.getBaselineComparisonMetricsJson());
                if (root.has("regressionDetected")) {
                    return root.get("regressionDetected").asBoolean();
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
