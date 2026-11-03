package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.ContextPerformanceProfileDto;
import com.aquaai.prediction.domain.model.AgriculturalSeason;
import com.aquaai.prediction.domain.model.ContextPerformanceProfile;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.TelemetryQualityLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.service.ContextualDriftDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetContextPerformanceProfilesUseCase {

    private final PredictionEvaluationRepository evaluationRepository;
    private final PredictionDataProvider dataProvider;
    private final PredictionMethodVersionRepository versionRepository;
    private final ContextualDriftDetector driftDetector = new ContextualDriftDetector();

    @Transactional(readOnly = true)
    public List<ContextPerformanceProfileDto> execute(PredictionType typeFilter, String versionTagFilter) {
        List<ContextPerformanceProfile> domainProfiles = getDomainProfiles(typeFilter, versionTagFilter);
        return domainProfiles.stream()
                .map(ContextPerformanceProfileDto::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, List<ContextPerformanceProfile>> getProfilesByVersion(PredictionType type) {
        List<ContextPerformanceProfile> domainProfiles = getDomainProfiles(type, null);
        return domainProfiles.stream()
                .collect(Collectors.groupingBy(ContextPerformanceProfile::versionTag));
    }

    @Transactional(readOnly = true)
    public List<ContextPerformanceProfile> getDomainProfiles(PredictionType typeFilter, String versionTagFilter) {
        Instant now = Instant.now();
        Instant from = now.minus(Duration.ofDays(180));

        List<PredictionEvaluation> evaluations = evaluationRepository.findEvaluationsForPeriod(null, from, now);
        List<FieldMetadata> fields = dataProvider.getAccessibleFields(null, true);
        Map<UUID, FieldMetadata> fieldMap = fields.stream()
                .collect(Collectors.toMap(FieldMetadata::fieldId, f -> f, (a, b) -> a));

        // Group evaluations by context key
        record ContextSliceKey(
                PredictionType predictionType,
                String versionTag,
                String cropType,
                String soilType,
                String irrigationMethod,
                AgriculturalSeason season
        ) {}

        Map<ContextSliceKey, List<PredictionEvaluation>> groupedEvals = new HashMap<>();
        Map<String, List<PredictionEvaluation>> evalsByVersion = new HashMap<>();

        for (PredictionEvaluation eval : evaluations) {
            if (typeFilter != null && eval.getPredictionType() != typeFilter) {
                continue;
            }
            String vTag = eval.getModelVersion() != null ? eval.getModelVersion() : "DEFAULT";
            if (versionTagFilter != null && !versionTagFilter.equalsIgnoreCase(vTag)) {
                continue;
            }

            evalsByVersion.computeIfAbsent(vTag, k -> new ArrayList<>()).add(eval);

            FieldMetadata field = eval.getFieldId() != null ? fieldMap.get(eval.getFieldId()) : null;
            String crop = field != null && field.cropType() != null ? field.cropType().toUpperCase() : "COTTON";
            String soil = field != null && field.soilType() != null ? field.soilType().toUpperCase() : "LOAM";
            String method = field != null && field.irrigationMethod() != null ? field.irrigationMethod().toUpperCase() : "DRIP";
            AgriculturalSeason season = eval.getTargetTime() != null
                    ? AgriculturalSeason.fromInstant(eval.getTargetTime())
                    : AgriculturalSeason.SUMMER;

            ContextSliceKey key = new ContextSliceKey(eval.getPredictionType(), vTag, crop, soil, method, season);
            groupedEvals.computeIfAbsent(key, k -> new ArrayList<>()).add(eval);
        }

        // Global metrics by version tag
        Map<String, BigDecimal> globalMaeByVersion = new HashMap<>();
        Map<String, BigDecimal> globalMbeByVersion = new HashMap<>();
        for (Map.Entry<String, List<PredictionEvaluation>> entry : evalsByVersion.entrySet()) {
            List<PredictionEvaluation> versionEvals = entry.getValue();
            BigDecimal sumAbsErr = BigDecimal.ZERO;
            BigDecimal sumDirErr = BigDecimal.ZERO;
            int count = 0;
            for (PredictionEvaluation e : versionEvals) {
                if (e.getAbsoluteError() != null) {
                    sumAbsErr = sumAbsErr.add(e.getAbsoluteError());
                    sumDirErr = sumDirErr.add(e.getDirectionalError() != null ? e.getDirectionalError() : BigDecimal.ZERO);
                    count++;
                }
            }
            if (count > 0) {
                globalMaeByVersion.put(entry.getKey(), sumAbsErr.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
                globalMbeByVersion.put(entry.getKey(), sumDirErr.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            }
        }

        List<ContextPerformanceProfile> results = new ArrayList<>();

        for (Map.Entry<ContextSliceKey, List<PredictionEvaluation>> entry : groupedEvals.entrySet()) {
            ContextSliceKey key = entry.getKey();
            List<PredictionEvaluation> evals = entry.getValue();
            int n = evals.size();

            BigDecimal sumAbs = BigDecimal.ZERO;
            BigDecimal sumDir = BigDecimal.ZERO;
            BigDecimal sumSq = BigDecimal.ZERO;
            int accurateCount = 0;
            int validCount = 0;

            for (PredictionEvaluation e : evals) {
                if (e.getAbsoluteError() != null) {
                    sumAbs = sumAbs.add(e.getAbsoluteError());
                    sumDir = sumDir.add(e.getDirectionalError() != null ? e.getDirectionalError() : BigDecimal.ZERO);
                    BigDecimal err = e.getAbsoluteError();
                    sumSq = sumSq.add(err.multiply(err));
                    validCount++;
                }
                boolean isAccurate = (e.getIsWithinBounds() != null && e.getIsWithinBounds())
                        || e.getClassificationResult() == com.aquaai.prediction.domain.model.PredictionClassificationResult.TRUE_POSITIVE
                        || e.getClassificationResult() == com.aquaai.prediction.domain.model.PredictionClassificationResult.TRUE_NEGATIVE
                        || (e.getRelativeErrorPercent() != null && e.getRelativeErrorPercent().compareTo(BigDecimal.valueOf(15.0)) <= 0);
                if (isAccurate) {
                    accurateCount++;
                }
            }

            BigDecimal mae = validCount > 0
                    ? sumAbs.divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP)
                    : null;
            BigDecimal mbe = validCount > 0
                    ? sumDir.divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP)
                    : null;
            BigDecimal rmse = validCount > 0
                    ? BigDecimal.valueOf(Math.sqrt(sumSq.divide(BigDecimal.valueOf(validCount), 4, RoundingMode.HALF_UP).doubleValue()))
                            .setScale(2, RoundingMode.HALF_UP)
                    : null;
            BigDecimal accuracy = n > 0
                    ? BigDecimal.valueOf(accurateCount * 100.0 / n).setScale(1, RoundingMode.HALF_UP)
                    : null;

            ContextPerformanceProfile rawProfile = ContextPerformanceProfile.create(
                    key.predictionType(),
                    key.versionTag(),
                    key.cropType(),
                    key.soilType(),
                    key.irrigationMethod(),
                    TelemetryQualityLevel.HIGH,
                    key.season(),
                    n,
                    mae,
                    rmse,
                    mbe,
                    accuracy,
                    ModelHealthStatus.HEALTHY
            );

            // Contextual drift check against version's global benchmark
            BigDecimal globalMae = globalMaeByVersion.get(key.versionTag());
            BigDecimal globalMbe = globalMbeByVersion.get(key.versionTag());
            ContextPerformanceProfile checkedProfile = driftDetector.detectDrift(rawProfile, globalMae, globalMbe);
            results.add(checkedProfile);
        }

        // Ensure active and governed versions have baseline representation even if empirical counts are low
        List<PredictionMethodVersion> versions = versionRepository.findAll();
        for (PredictionMethodVersion version : versions) {
            if (typeFilter != null && version.getPredictionType() != typeFilter) {
                continue;
            }
            if (versionTagFilter != null && !versionTagFilter.equalsIgnoreCase(version.getVersionTag())) {
                continue;
            }

            boolean alreadyPresent = results.stream()
                    .anyMatch(r -> r.versionTag().equalsIgnoreCase(version.getVersionTag()));
            if (!alreadyPresent) {
                // Synthesize an uncalibrated / insufficient data profile
                results.add(ContextPerformanceProfile.create(
                        version.getPredictionType(),
                        version.getVersionTag(),
                        "ALL",
                        "ALL",
                        "ALL",
                        TelemetryQualityLevel.UNKNOWN,
                        AgriculturalSeason.SUMMER,
                        0,
                        null,
                        null,
                        null,
                        null,
                        ModelHealthStatus.INSUFFICIENT_DATA
                ));
            }
        }

        return results;
    }
}
