package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.TrainPredictionModelRequest;
import com.aquaai.prediction.application.dto.TrainPredictionModelResponse;
import com.aquaai.prediction.domain.model.DatasetSplit;
import com.aquaai.prediction.domain.model.FeatureRecord;
import com.aquaai.prediction.domain.model.FeatureSchemaVersion;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.model.PredictionTrainingDataset;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.TrainingDatasetQuality;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.port.PredictionModelCandidateRepository;
import com.aquaai.prediction.domain.port.PredictionTrainingDatasetRepository;
import com.aquaai.prediction.domain.service.DatasetQualityGate;
import com.aquaai.prediction.domain.service.DatasetQualityGate.QualityGateResult;
import com.aquaai.prediction.domain.service.DatasetQualityGate.RawObservationSample;
import com.aquaai.prediction.domain.service.FeatureExtractor;
import com.aquaai.prediction.domain.service.RidgeRegressionTrainer;
import com.aquaai.prediction.domain.service.RidgeRegressionTrainer.TrainedModelResult;
import com.aquaai.prediction.domain.service.TemporalDataSplitter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Use Case initiating controlled, offline training of ML model candidates.
 * Enforces evidence-based learning, zero online self-training, and rigorous sample gating.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainPredictionModelUseCase {

    public static final int MINIMUM_TRAINING_SAMPLE_GATE = 30;

    private final PredictionDataProvider dataProvider;
    private final PredictionTrainingDatasetRepository datasetRepository;
    private final PredictionModelCandidateRepository candidateRepository;
    private final PredictionMethodVersionRepository versionRepository;
    private final GovernanceAuditLogRepository auditLogRepository;

    private final DatasetQualityGate qualityGate = new DatasetQualityGate();
    private final FeatureExtractor featureExtractor = new FeatureExtractor();
    private final TemporalDataSplitter dataSplitter = new TemporalDataSplitter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public TrainPredictionModelResponse execute(TrainPredictionModelRequest request, String actor, String role) {
        PredictionType predictionType = request.predictionType() != null ? request.predictionType() : PredictionType.SOIL_MOISTURE_FORECAST;
        Instant now = Instant.now();
        Instant periodStart = request.periodStart() != null ? request.periodStart() : now.minus(Duration.ofDays(90));
        Instant periodEnd = request.periodEnd() != null ? request.periodEnd() : now;

        log.info("Initiating controlled model training for {} in window [{} to {}] requested by {}",
                predictionType, periodStart, periodEnd, actor);

        // 1. Resolve accessible fields
        List<FieldMetadata> fields;
        if (request.fieldId() != null) {
            Optional<FieldMetadata> fieldOpt = dataProvider.getFieldMetadata(request.fieldId());
            fields = fieldOpt.map(List::of).orElse(List.of());
        } else {
            fields = dataProvider.getAccessibleFields(null, true);
        }

        if (fields.isEmpty()) {
            return handleInsufficientData(predictionType, periodStart, periodEnd, "No registered agricultural fields found in target scope.", actor, 0);
        }

        // 2. Extract historical feature and target observation pairs
        List<RawObservationSample> rawSamples = collectHistoricalSamples(fields, periodStart, periodEnd);

        // 3. Evaluate Data Quality Gate
        QualityGateResult qualityResult = qualityGate.filterAndValidate(rawSamples, MINIMUM_TRAINING_SAMPLE_GATE);
        TrainingDatasetQuality quality = qualityResult.quality();
        List<FeatureRecord> validRecords = qualityResult.validRecords();

        String qualitySummaryJson = serializeQualitySummary(quality);

        if (!quality.isSufficientForTraining(MINIMUM_TRAINING_SAMPLE_GATE)) {
            String reason = quality.rejectionReasons().isEmpty()
                    ? "Verified sample count (" + validRecords.size() + ") is below minimum gate (N >= " + MINIMUM_TRAINING_SAMPLE_GATE + ")."
                    : String.join("; ", quality.rejectionReasons());

            return handleInsufficientData(predictionType, periodStart, periodEnd, reason, actor, validRecords.size());
        }

        // 4. Compute Canonical Dataset Hash
        String datasetHash = computeDatasetHash(validRecords);

        // 5. Persist Training Dataset Snapshot
        PredictionTrainingDataset datasetSnapshot = PredictionTrainingDataset.create(
                predictionType,
                datasetHash,
                FeatureSchemaVersion.SOIL_MOISTURE_SCHEMA_V1,
                periodStart,
                periodEnd,
                request.fieldId() != null ? request.fieldId().toString() : "ALL",
                validRecords.size(),
                quality,
                qualitySummaryJson,
                actor != null ? actor : "ADMIN"
        );
        PredictionTrainingDataset savedDataset = datasetRepository.save(datasetSnapshot);

        // 6. Chronological Non-Overlapping Temporal Split (60% Train, 20% Val, 20% Test)
        DatasetSplit split = dataSplitter.split(validRecords);

        // 7. Train JVM-Native Regularized Ridge Regression Model
        double lambda = request.l2Penalty() != null ? request.l2Penalty() : 1.0;
        RidgeRegressionTrainer trainer = new RidgeRegressionTrainer(lambda);
        TrainedModelResult trainedResult = trainer.train(split);

        // 8. Generate Next Unique Model Version Tag
        List<PredictionModelCandidate> existingCandidates = candidateRepository.findByPredictionType(predictionType);
        int nextVerNum = existingCandidates.size() + 1;
        String versionTag = "SOIL_MOISTURE_ML_V" + nextVerNum;

        // 9. Serialize Metrics JSON
        String trainMetricsJson = serializeMetrics(trainedResult.trainMetrics());
        String valMetricsJson = serializeMetrics(trainedResult.validationMetrics());
        String testMetricsJson = serializeMetrics(trainedResult.testMetrics());
        String baselineComparisonJson = serializeBaselineComparison(trainedResult.baselineComparison());

        // 10. Persist Governed Model Candidate (Status: CANDIDATE)
        PredictionModelCandidate candidate = PredictionModelCandidate.builder()
                .id(UUID.randomUUID())
                .predictionType(predictionType)
                .modelType("REGRESSION_RIDGE_HYBRID")
                .versionTag(versionTag)
                .datasetSnapshotId(savedDataset.getId())
                .featureSchemaVersion(FeatureSchemaVersion.SOIL_MOISTURE_SCHEMA_V1)
                .trainingPeriodStart(split.trainPeriodStart())
                .trainingPeriodEnd(split.trainPeriodEnd())
                .validationPeriodStart(split.validationPeriodStart())
                .validationPeriodEnd(split.validationPeriodEnd())
                .testPeriodStart(split.testPeriodStart())
                .testPeriodEnd(split.testPeriodEnd())
                .trainingSampleCount(split.trainRecords().size())
                .validationSampleCount(split.validationRecords().size())
                .testSampleCount(split.testRecords().size())
                .trainingMetricsJson(trainMetricsJson)
                .validationMetricsJson(valMetricsJson)
                .testMetricsJson(testMetricsJson)
                .baselineComparisonMetricsJson(baselineComparisonJson)
                .configurationSnapshotJson(trainedResult.configurationJson())
                .artifactHash(trainedResult.artifactHash())
                .status(MethodVersionStatus.CANDIDATE)
                .isImmutable(false)
                .createdAt(now)
                .createdBy(actor != null ? actor : "ADMIN")
                .optLockVersion(0L)
                .build();

        PredictionModelCandidate savedCandidate = candidateRepository.save(candidate);

        // 11. Register in PredictionMethodVersion repository for governance & adaptive intelligence
        PredictionMethodVersion methodVersion = PredictionMethodVersion.createCandidate(
                predictionType,
                versionTag,
                "Trained Ridge regression hybrid ML candidate on dataset snapshot " + datasetHash.substring(0, 8),
                trainedResult.configurationJson(),
                actor != null ? actor : "ADMIN"
        );
        versionRepository.save(methodVersion);

        // 12. Record Governance Audit Trail
        auditLogRepository.save(new GovernanceAuditLog(
                UUID.randomUUID(),
                GovernanceAction.PROPOSAL_CREATED,
                predictionType,
                versionTag,
                null,
                actor != null ? actor : "ADMIN",
                role != null ? role : "ADMIN",
                now,
                "Controlled training run produced candidate " + versionTag + " with " + validRecords.size() + " samples.",
                "NONE",
                MethodVersionStatus.CANDIDATE.name(),
                String.format("{\"datasetSnapshotId\":\"%s\",\"artifactHash\":\"%s\",\"testMae\":%.2f,\"baselineMae\":%.2f}",
                        savedDataset.getId(), trainedResult.artifactHash(), trainedResult.testMetrics().mae(), trainedResult.baselineTestMetrics().mae())
        ));

        log.info("Successfully completed training for candidate {}: test MAE=%.2f, baseline MAE=%.2f, improvement=%.1f%%",
                versionTag, trainedResult.testMetrics().mae(), trainedResult.baselineTestMetrics().mae(),
                trainedResult.baselineComparison().maeImprovementPercent());

        return new TrainPredictionModelResponse(
                "SUCCESS",
                "Model candidate " + versionTag + " successfully trained and submitted to governance review.",
                predictionType,
                savedDataset.getId(),
                savedDataset.getDatasetHash(),
                validRecords.size(),
                MINIMUM_TRAINING_SAMPLE_GATE,
                savedCandidate.getId(),
                versionTag,
                trainedResult.baselineComparison().regressionReason(),
                trainedResult.baselineTestMetrics().mae(),
                trainedResult.testMetrics().mae(),
                trainedResult.baselineComparison().maeImprovementPercent(),
                trainedResult.baselineComparison().regressionDetected()
        );
    }

    private TrainPredictionModelResponse handleInsufficientData(
            PredictionType type,
            Instant start,
            Instant end,
            String reason,
            String actor,
            int sampleCount
    ) {
        log.warn("Model training aborted due to insufficient training data: {}", reason);
        PredictionTrainingDataset dataset = PredictionTrainingDataset.create(
                type,
                "HASH_INSUFFICIENT_DATA",
                FeatureSchemaVersion.SOIL_MOISTURE_SCHEMA_V1,
                start,
                end,
                "ALL",
                sampleCount,
                TrainingDatasetQuality.empty(reason),
                "{\"status\":\"INSUFFICIENT_DATA\",\"reason\":\"" + reason.replace("\"", "'") + "\"}",
                actor != null ? actor : "ADMIN"
        );
        PredictionTrainingDataset saved = datasetRepository.save(dataset);

        return TrainPredictionModelResponse.insufficientData(
                type,
                saved.getId(),
                saved.getDatasetHash(),
                sampleCount,
                MINIMUM_TRAINING_SAMPLE_GATE,
                reason
        );
    }

    private List<RawObservationSample> collectHistoricalSamples(List<FieldMetadata> fields, Instant start, Instant end) {
        List<RawObservationSample> samples = new ArrayList<>();

        for (FieldMetadata field : fields) {
            List<TelemetryObservation> obs = dataProvider.getMoistureObservationsInWindow(field.fieldId(), start, end);
            if (obs.size() < 2) continue;

            obs.sort(Comparator.comparing(TelemetryObservation::measuredAt));
            WeatherContext weather = dataProvider.getWeatherContext(field.fieldId());
            WaterBalance waterBalance = dataProvider.getWaterBalance(field.fieldId()).orElse(null);

            // Match historical forecast points with ground-truth target at +24h
            for (int i = 0; i < obs.size(); i++) {
                TelemetryObservation currentObs = obs.get(i);
                Instant tForecast = currentObs.measuredAt();
                Instant tTarget = tForecast.plus(Duration.ofHours(24));

                if (tTarget.isAfter(end)) {
                    continue; // Future target beyond window
                }

                // Look for ground truth outcome near tTarget (+/- 2 hours)
                Double targetVal = null;
                for (int j = i + 1; j < obs.size(); j++) {
                    TelemetryObservation next = obs.get(j);
                    long diffMinutes = Math.abs(Duration.between(next.measuredAt(), tTarget).toMinutes());
                    if (diffMinutes <= 120 && !"INVALID".equalsIgnoreCase(next.quality())) {
                        targetVal = next.value().doubleValue();
                        break;
                    }
                }

                if (targetVal == null) {
                    continue; // Skip if no ground truth outcome recorded
                }

                // Sublist of past observations up to tForecast
                List<TelemetryObservation> pastObs = obs.subList(0, i + 1);

                BigDecimal recentIrrigation = dataProvider.getActualWaterConsumptionInWindow(
                        field.fieldId(), tForecast.minus(Duration.ofDays(7)), tForecast
                );

                double[] features = featureExtractor.extractSoilMoistureFeatures(
                        field, pastObs, weather, waterBalance, recentIrrigation, tForecast
                );

                double baselineSlope = features[1];
                double baselinePred = Math.max(5.0, Math.min(55.0, features[0] + (baselineSlope * 24.0)));

                samples.add(new RawObservationSample(
                        UUID.randomUUID(),
                        field.fieldId(),
                        tForecast,
                        features,
                        targetVal,
                        baselinePred,
                        currentObs.quality()
                ));
            }
        }

        return samples;
    }

    private String computeDatasetHash(List<FeatureRecord> records) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (FeatureRecord r : records) {
                String line = r.fieldId() + ";" + r.timestamp().getEpochSecond() + ";" + r.targetValue();
                md.update(line.getBytes(StandardCharsets.UTF_8));
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "HASH_" + UUID.randomUUID();
        }
    }

    private String serializeQualitySummary(TrainingDatasetQuality q) {
        try {
            return objectMapper.writeValueAsString(q);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String serializeMetrics(com.aquaai.prediction.domain.model.ModelMetrics m) {
        try {
            return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String serializeBaselineComparison(com.aquaai.prediction.domain.model.BaselineComparisonMetrics c) {
        try {
            return objectMapper.writeValueAsString(c);
        } catch (Exception e) {
            return "{}";
        }
    }
}
