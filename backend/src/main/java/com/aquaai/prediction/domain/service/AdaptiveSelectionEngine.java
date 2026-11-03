package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.AdaptiveCandidateEvaluation;
import com.aquaai.prediction.domain.model.AdaptiveSelectionResult;
import com.aquaai.prediction.domain.model.AnomalySeverityContext;
import com.aquaai.prediction.domain.model.ContextMatchLevel;
import com.aquaai.prediction.domain.model.ContextPerformanceProfile;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.PredictionContext;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.SensorAvailabilityStatus;
import com.aquaai.prediction.domain.model.TelemetryQualityLevel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authoritative, deterministic context-aware adaptive selection engine.
 * <p>
 * Evaluates candidate governed prediction method versions against the parcel operational context
 * strictly adhering to the 9-level selection hierarchy.
 */
public class AdaptiveSelectionEngine {

    public AdaptiveSelectionResult selectBestMethod(
            PredictionType predictionType,
            PredictionContext context,
            List<PredictionMethodVersion> availableVersions,
            Map<String, List<ContextPerformanceProfile>> versionProfiles
    ) {
        Instant now = Instant.now();

        // 1. Safe Fallback Resolution
        PredictionMethodVersion activeBaseline = availableVersions.stream()
                .filter(v -> v.getPredictionType() == predictionType && v.getStatus() == MethodVersionStatus.ACTIVE)
                .findFirst()
                .orElseGet(() -> availableVersions.stream()
                        .filter(v -> v.getPredictionType() == predictionType)
                        .findFirst()
                        .orElse(null));

        String baselineTag = activeBaseline != null ? activeBaseline.getVersionTag() : predictionType.name() + "_V1";
        String baselineConfig = activeBaseline != null ? activeBaseline.getConfigurationJson() : "{}";

        // -------------------------------------------------------------
        // Level 1: SAFETY ABSOLUTE PRIORITY
        // -------------------------------------------------------------
        if (context.recentAnomalySeverity() == AnomalySeverityContext.CRITICAL) {
            return AdaptiveSelectionResult.builder()
                    .predictionType(predictionType)
                    .fieldId(context.fieldId())
                    .context(context)
                    .selectedMethod(predictionType.name() + "_SAFETY_INTERLOCK_DOMINANT")
                    .selectedVersionTag(baselineTag)
                    .configurationSnapshot(baselineConfig)
                    .contextMatchLevel(ContextMatchLevel.BASELINE_FALLBACK)
                    .sampleSize(0)
                    .selectionReason("Hydraulic safety interlock active on parcel. Fallback to safety-dominant baseline.")
                    .selectionScore(BigDecimal.valueOf(100.0))
                    .confidencePercent(90)
                    .evidenceSummary("Active CRITICAL hydraulic anomaly detected; emergency safety interlock takes absolute precedence.")
                    .alternativesConsidered(List.of())
                    .limitations("Operational execution blocked until hydraulic safety issue is cleared.")
                    .selectedAt(now)
                    .build();
        }

        // -------------------------------------------------------------
        // Level 4 Check: DATA SUFFICIENCY & REQUIRED SENSORS
        // -------------------------------------------------------------
        if (context.sensorAvailability() == SensorAvailabilityStatus.MISSING
                && predictionType == PredictionType.SOIL_MOISTURE_FORECAST) {
            return AdaptiveSelectionResult.builder()
                    .predictionType(predictionType)
                    .fieldId(context.fieldId())
                    .context(context)
                    .selectedMethod(predictionType.name() + "_INSUFFICIENT_DATA")
                    .selectedVersionTag(baselineTag)
                    .configurationSnapshot(baselineConfig)
                    .contextMatchLevel(ContextMatchLevel.INSUFFICIENT_DATA)
                    .sampleSize(0)
                    .selectionReason("Required soil moisture sensors missing on parcel. Reverting to safe default.")
                    .selectionScore(BigDecimal.ZERO)
                    .confidencePercent(20)
                    .evidenceSummary("No physical sensor telemetry available for empirical prediction.")
                    .alternativesConsidered(List.of())
                    .limitations("Predictions generated without sensor telemetry have low confidence.")
                    .selectedAt(now)
                    .build();
        }

        // -------------------------------------------------------------
        // Level 2 & 3: GOVERNANCE ELIGIBILITY FILTER
        // -------------------------------------------------------------
        List<AdaptiveCandidateEvaluation> candidateEvaluations = new ArrayList<>();
        List<EligibleCandidate> eligibleCandidates = new ArrayList<>();

        for (PredictionMethodVersion version : availableVersions) {
            if (version.getPredictionType() != predictionType) continue;

            String vTag = version.getVersionTag();
            MethodVersionStatus status = version.getStatus();

            // Governance eligibility check: ONLY ACTIVE, APPROVED, or SHADOW
            if (status != MethodVersionStatus.ACTIVE
                    && status != MethodVersionStatus.APPROVED
                    && status != MethodVersionStatus.SHADOW) {
                candidateEvaluations.add(AdaptiveCandidateEvaluation.ineligible(
                        vTag, status, "Governance status '" + status + "' is not eligible for production selection."
                ));
                continue;
            }

            // Find matching profiles for this version
            List<ContextPerformanceProfile> profiles = versionProfiles != null
                    ? versionProfiles.getOrDefault(vTag, List.of())
                    : List.of();

            MatchResult match = findBestContextMatch(context, profiles);

            if (match.profile == null || !match.profile.hasSufficientSample()) {
                candidateEvaluations.add(AdaptiveCandidateEvaluation.builder()
                        .versionTag(vTag)
                        .status(status)
                        .isEligible(true)
                        .contextMatchLevel(match.matchLevel)
                        .sampleSize(match.profile != null ? match.profile.sampleSize() : 0)
                        .hasSufficientSample(false)
                        .rejectionReason("Insufficient contextual sample size (n < 10)")
                        .build());
                continue;
            }

            ContextPerformanceProfile p = match.profile;

            // Level 8 Check: RECENT PERFORMANCE & CONTEXTUAL DRIFT
            if (p.contextualModelHealth() == ModelHealthStatus.DEGRADED) {
                candidateEvaluations.add(AdaptiveCandidateEvaluation.builder()
                        .versionTag(vTag)
                        .status(status)
                        .isEligible(false)
                        .ineligibilityReason("Contextual model health is DEGRADED for this context (" + p.driftDiagnosis() + ")")
                        .contextMatchLevel(match.matchLevel)
                        .sampleSize(p.sampleSize())
                        .hasSufficientSample(true)
                        .meanAbsoluteError(p.meanAbsoluteError())
                        .meanBiasError(p.meanBiasError())
                        .contextualHealth(p.contextualModelHealth())
                        .rejectionReason("Contextual drift detected: " + p.driftDiagnosis())
                        .build());
                continue;
            }

            // Compute deterministic selection score
            BigDecimal score = computeSelectionScore(match.matchLevel, p, status);

            AdaptiveCandidateEvaluation eval = AdaptiveCandidateEvaluation.builder()
                    .versionTag(vTag)
                    .status(status)
                    .isEligible(true)
                    .contextMatchLevel(match.matchLevel)
                    .sampleSize(p.sampleSize())
                    .hasSufficientSample(true)
                    .meanAbsoluteError(p.meanAbsoluteError())
                    .meanBiasError(p.meanBiasError())
                    .contextualHealth(p.contextualModelHealth())
                    .selectionScore(score)
                    .build();

            candidateEvaluations.add(eval);
            eligibleCandidates.add(new EligibleCandidate(version, eval, p, match.matchLevel));
        }

        // -------------------------------------------------------------
        // Level 5, 6, 7: SELECTION RANKING
        // -------------------------------------------------------------
        if (!eligibleCandidates.isEmpty()) {
            // Sort by: 1) Match level precedence, 2) Selection score (descending), 3) Sample size (descending)
            eligibleCandidates.sort(Comparator
                    .comparingInt((EligibleCandidate c) -> matchLevelRank(c.matchLevel))
                    .thenComparing((EligibleCandidate c) -> c.evaluation.selectionScore(), Comparator.reverseOrder())
                    .thenComparing((EligibleCandidate c) -> c.profile.sampleSize(), Comparator.reverseOrder()));

            EligibleCandidate best = eligibleCandidates.get(0);
            PredictionMethodVersion chosenVersion = best.version;
            ContextPerformanceProfile chosenProfile = best.profile;

            int confidence = calculateAdaptiveConfidence(best.matchLevel, chosenProfile, context.telemetryQuality());

            String reason = String.format("Method %s selected based on %s with n=%d evaluations (MAE: %.1f%%, bias: %s%.1f%%).",
                    chosenVersion.getVersionTag(),
                    best.matchLevel.name().replace('_', ' ').toLowerCase(),
                    chosenProfile.sampleSize(),
                    chosenProfile.meanAbsoluteError() != null ? chosenProfile.meanAbsoluteError() : BigDecimal.ZERO,
                    chosenProfile.meanBiasError() != null && chosenProfile.meanBiasError().compareTo(BigDecimal.ZERO) > 0 ? "+" : "",
                    chosenProfile.meanBiasError() != null ? chosenProfile.meanBiasError() : BigDecimal.ZERO);

            String evidence = String.format("Validated for %s + %s under %s season. Historical MAE: %.1f%% across %d evaluations.",
                    context.cropType(), context.irrigationMethod(), context.season(),
                    chosenProfile.meanAbsoluteError() != null ? chosenProfile.meanAbsoluteError() : BigDecimal.ZERO,
                    chosenProfile.sampleSize());

            return AdaptiveSelectionResult.builder()
                    .predictionType(predictionType)
                    .fieldId(context.fieldId())
                    .context(context)
                    .selectedMethod(predictionType.name() + "_CONTEXT_OPTIMIZED")
                    .selectedVersionTag(chosenVersion.getVersionTag())
                    .configurationSnapshot(chosenVersion.getConfigurationJson())
                    .contextMatchLevel(best.matchLevel)
                    .sampleSize(chosenProfile.sampleSize())
                    .selectionReason(reason)
                    .selectionScore(best.evaluation.selectionScore())
                    .confidencePercent(confidence)
                    .evidenceSummary(evidence)
                    .alternativesConsidered(candidateEvaluations)
                    .limitations(best.matchLevel != ContextMatchLevel.EXACT_CONTEXT
                            ? "Selected via partial/similar context match; verify against field observations."
                            : "Validated empirical performance within normal operational limits.")
                    .selectedAt(now)
                    .build();
        }

        // -------------------------------------------------------------
        // Level 9: DEFAULT BASELINE FALLBACK
        // -------------------------------------------------------------
        String fallbackReason = "No candidate method has sufficient context-specific evidence (n >= 10). Active baseline "
                + baselineTag + " selected as safe default.";

        return AdaptiveSelectionResult.builder()
                .predictionType(predictionType)
                .fieldId(context.fieldId())
                .context(context)
                .selectedMethod(predictionType.name() + "_BASELINE")
                .selectedVersionTag(baselineTag)
                .configurationSnapshot(baselineConfig)
                .contextMatchLevel(ContextMatchLevel.BASELINE_FALLBACK)
                .sampleSize(0)
                .selectionReason(fallbackReason)
                .selectionScore(BigDecimal.valueOf(50.0))
                .confidencePercent(70)
                .evidenceSummary("Fallback to active production baseline. Context evidence below statistical significance threshold.")
                .alternativesConsidered(candidateEvaluations)
                .limitations("Contextual tailoring unavailable due to limited historical sample count.")
                .selectedAt(now)
                .build();
    }

    private record MatchResult(ContextPerformanceProfile profile, ContextMatchLevel matchLevel) {}

    private MatchResult findBestContextMatch(
            PredictionContext context,
            List<ContextPerformanceProfile> profiles
    ) {
        if (profiles.isEmpty()) {
            return new MatchResult(null, ContextMatchLevel.BASELINE_FALLBACK);
        }

        String crop = context.cropType();
        String soil = context.soilType();
        String method = context.irrigationMethod();
        TelemetryQualityLevel telem = context.telemetryQuality();

        // 1. Try Exact Match (crop + soil + method + telemetry)
        Optional<ContextPerformanceProfile> exact = profiles.stream()
                .filter(p -> crop.equalsIgnoreCase(p.cropType())
                        && soil.equalsIgnoreCase(p.soilType())
                        && method.equalsIgnoreCase(p.irrigationMethod())
                        && telem == p.telemetryQuality()
                        && p.hasSufficientSample())
                .findFirst();

        if (exact.isPresent()) {
            return new MatchResult(exact.get(), ContextMatchLevel.EXACT_CONTEXT);
        }

        // 2. Try Core Partial Match (crop + method)
        Optional<ContextPerformanceProfile> partial = profiles.stream()
                .filter(p -> crop.equalsIgnoreCase(p.cropType())
                        && method.equalsIgnoreCase(p.irrigationMethod())
                        && p.hasSufficientSample())
                .findFirst();

        if (partial.isPresent()) {
            return new MatchResult(partial.get(), ContextMatchLevel.PARTIAL_CONTEXT);
        }

        // 3. Try Crop-only Match
        Optional<ContextPerformanceProfile> cropMatch = profiles.stream()
                .filter(p -> crop.equalsIgnoreCase(p.cropType()) && p.hasSufficientSample())
                .findFirst();

        if (cropMatch.isPresent()) {
            return new MatchResult(cropMatch.get(), ContextMatchLevel.SIMILAR_CONTEXT);
        }

        // 4. Try Global Profile
        Optional<ContextPerformanceProfile> global = profiles.stream()
                .filter(p -> "ALL".equalsIgnoreCase(p.cropType()) && p.hasSufficientSample())
                .findFirst();

        return global.map(contextPerformanceProfile -> new MatchResult(contextPerformanceProfile, ContextMatchLevel.BASELINE_FALLBACK))
                .orElseGet(() -> new MatchResult(null, ContextMatchLevel.BASELINE_FALLBACK));
    }

    private BigDecimal computeSelectionScore(
            ContextMatchLevel matchLevel,
            ContextPerformanceProfile profile,
            MethodVersionStatus status
    ) {
        // Base match score: EXACT=40, PARTIAL=30, SIMILAR=20, BASELINE=10
        int baseMatch = switch (matchLevel) {
            case EXACT_CONTEXT -> 40;
            case PARTIAL_CONTEXT -> 30;
            case SIMILAR_CONTEXT -> 20;
            default -> 10;
        };

        // Accuracy score component (max 40):
        // If MAE available, lower MAE yields higher score: max(0, 40 - MAE * 1.5)
        double errorComponent = 20.0;
        if (profile.meanAbsoluteError() != null) {
            double mae = profile.meanAbsoluteError().doubleValue();
            errorComponent = Math.max(0.0, 40.0 - (mae * 1.5));
        } else if (profile.accuracyPercent() != null) {
            errorComponent = (profile.accuracyPercent().doubleValue() / 100.0) * 40.0;
        }

        // Sample size confidence boost (max 10): min(10, sampleSize / 5)
        double sampleBoost = Math.min(10.0, (double) profile.sampleSize() / 5.0);

        // Status bonus: ACTIVE gets +10 bonus for production stability
        double statusBonus = status == MethodVersionStatus.ACTIVE ? 10.0 : 5.0;

        double total = baseMatch + errorComponent + sampleBoost + statusBonus;
        return BigDecimal.valueOf(Math.min(100.0, Math.max(0.0, total))).setScale(1, RoundingMode.HALF_UP);
    }

    private int matchLevelRank(ContextMatchLevel level) {
        return switch (level) {
            case EXACT_CONTEXT -> 1;
            case PARTIAL_CONTEXT -> 2;
            case SIMILAR_CONTEXT -> 3;
            case BASELINE_FALLBACK -> 4;
            case INSUFFICIENT_DATA -> 5;
        };
    }

    private int calculateAdaptiveConfidence(
            ContextMatchLevel matchLevel,
            ContextPerformanceProfile profile,
            TelemetryQualityLevel telemetry
    ) {
        int base = switch (matchLevel) {
            case EXACT_CONTEXT -> 88;
            case PARTIAL_CONTEXT -> 80;
            case SIMILAR_CONTEXT -> 72;
            default -> 65;
        };

        if (telemetry == TelemetryQualityLevel.HIGH) base += 5;
        if (telemetry == TelemetryQualityLevel.LOW) base -= 10;
        if (telemetry == TelemetryQualityLevel.INSUFFICIENT) base -= 20;

        if (profile.contextualModelHealth() == ModelHealthStatus.WATCH) base -= 8;

        return Math.max(20, Math.min(98, base));
    }

    private record EligibleCandidate(
            PredictionMethodVersion version,
            AdaptiveCandidateEvaluation evaluation,
            ContextPerformanceProfile profile,
            ContextMatchLevel matchLevel
    ) {}
}
