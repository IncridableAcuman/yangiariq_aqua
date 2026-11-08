package com.aquaai.performance.domain.service;

import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.EvidenceClassification;

/**
 * Pure domain service evaluating causal attribution between operational interventions and measured outcomes.
 * <p>
 * Non-negotiable principles:
 * 1. Outputs are never treated as outcomes.
 * 2. Correlation is never treated as causation.
 * 3. Simulated or estimated baselines can NEVER yield DIRECTLY_VERIFIED claims.
 */
public class ImpactAttributionEngine {

    public AttributionLevel determineAttribution(
            BaselineQuality baselineQuality,
            EvidenceClassification postEvidenceClassification,
            boolean comparisonValid,
            boolean hasIndependentVerification,
            boolean hasConfoundingFactors,
            boolean isFavorableChange,
            boolean hasConflictingEvidence
    ) {
        // 1. Missing data or invalid comparison
        if (!comparisonValid || baselineQuality == BaselineQuality.INSUFFICIENT_BASELINE ||
                postEvidenceClassification == EvidenceClassification.MISSING) {
            return AttributionLevel.NOT_ASSESSABLE;
        }

        // 2. Conflicting evidence or no favorable outcome
        if (hasConflictingEvidence || !isFavorableChange) {
            return AttributionLevel.INCONCLUSIVE;
        }

        // 3. Simulated baseline or simulated post-intervention telemetry can NEVER be verified
        if (baselineQuality == BaselineQuality.SIMULATED_BASELINE ||
                postEvidenceClassification == EvidenceClassification.SIMULATED) {
            // Maximum attribution for simulated data is ASSOCIATED
            return AttributionLevel.ASSOCIATED;
        }

        // 4. Estimated baseline
        if (baselineQuality == BaselineQuality.ESTIMATED_BASELINE ||
                postEvidenceClassification == EvidenceClassification.ESTIMATED) {
            return AttributionLevel.ASSOCIATED;
        }

        // 5. Directly Verified requirements:
        // - VERIFIED_BASELINE
        // - OBSERVED or CALCULATED post-evidence
        // - Independent verification conducted
        // - No unmodeled confounding factors
        if (baselineQuality == BaselineQuality.VERIFIED_BASELINE &&
                (postEvidenceClassification == EvidenceClassification.OBSERVED ||
                 postEvidenceClassification == EvidenceClassification.CALCULATED) &&
                hasIndependentVerification &&
                !hasConfoundingFactors) {
            return AttributionLevel.DIRECTLY_VERIFIED;
        }

        // 6. Strongly Associated:
        // Good baseline and post-evidence, clear temporal link, but minor confounding factors or lack of separate 3rd party on-site audit
        if ((baselineQuality == BaselineQuality.VERIFIED_BASELINE || baselineQuality == BaselineQuality.PARTIALLY_VERIFIED_BASELINE) &&
                (postEvidenceClassification == EvidenceClassification.OBSERVED ||
                 postEvidenceClassification == EvidenceClassification.CALCULATED)) {
            return AttributionLevel.STRONGLY_ASSOCIATED;
        }

        return AttributionLevel.ASSOCIATED;
    }
}
