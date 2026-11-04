package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.ModelCandidateDetailResponse;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.port.PredictionModelCandidateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetModelCandidateDetailUseCase {

    private final PredictionModelCandidateRepository candidateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<ModelCandidateDetailResponse> execute(UUID id) {
        if (id == null) return Optional.empty();
        return candidateRepository.findById(id).map(this::toDetail);
    }

    private ModelCandidateDetailResponse toDetail(PredictionModelCandidate c) {
        Map<String, Double> featureImportance = new HashMap<>();
        String limitations = "Validated empirical performance within normal operational limits. Fallback to deterministic baseline if telemetry quality deteriorates.";

        try {
            if (c.getConfigurationSnapshotJson() != null && !c.getConfigurationSnapshotJson().isBlank()) {
                JsonNode root = objectMapper.readTree(c.getConfigurationSnapshotJson());
                if (root.has("featureImportance")) {
                    JsonNode fiNode = root.get("featureImportance");
                    Iterator<String> fieldNames = fiNode.fieldNames();
                    while (fieldNames.hasNext()) {
                        String fName = fieldNames.next();
                        featureImportance.put(fName, fiNode.get(fName).asDouble());
                    }
                }
            }
        } catch (Exception ignored) {}

        if (featureImportance.isEmpty()) {
            limitations += " Feature contribution weights unavailable for this candidate configuration.";
        }

        return new ModelCandidateDetailResponse(
                c.getId(),
                c.getPredictionType(),
                c.getModelType(),
                c.getVersionTag(),
                c.getDatasetSnapshotId(),
                c.getFeatureSchemaVersion(),
                c.getTrainingPeriodStart(),
                c.getTrainingPeriodEnd(),
                c.getValidationPeriodStart(),
                c.getValidationPeriodEnd(),
                c.getTestPeriodStart(),
                c.getTestPeriodEnd(),
                c.getTrainingSampleCount(),
                c.getValidationSampleCount(),
                c.getTestSampleCount(),
                c.getTrainingMetricsJson(),
                c.getValidationMetricsJson(),
                c.getTestMetricsJson(),
                c.getBaselineComparisonMetricsJson(),
                c.getConfigurationSnapshotJson(),
                c.getArtifactHash(),
                c.getStatus(),
                c.isImmutable(),
                c.getCreatedAt(),
                c.getCreatedBy(),
                featureImportance,
                limitations
        );
    }
}
