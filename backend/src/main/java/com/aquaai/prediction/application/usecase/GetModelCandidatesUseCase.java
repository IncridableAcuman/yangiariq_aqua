package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.ModelCandidateDto;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionModelCandidateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetModelCandidatesUseCase {

    private final PredictionModelCandidateRepository candidateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ModelCandidateDto> execute(PredictionType type) {
        List<PredictionModelCandidate> candidates = type != null
                ? candidateRepository.findByPredictionType(type)
                : candidateRepository.findAll();

        return candidates.stream().map(this::toDto).toList();
    }

    private ModelCandidateDto toDto(PredictionModelCandidate c) {
        Double testMae = null;
        Double baselineMae = null;
        Double improvementPercent = null;
        boolean regressionDetected = false;
        String recommendationStatus = "PENDING_REVIEW";

        try {
            if (c.getBaselineComparisonMetricsJson() != null && !c.getBaselineComparisonMetricsJson().isBlank()) {
                JsonNode root = objectMapper.readTree(c.getBaselineComparisonMetricsJson());
                if (root.has("mlMae")) testMae = root.get("mlMae").asDouble();
                if (root.has("baselineMae")) baselineMae = root.get("baselineMae").asDouble();
                if (root.has("maeImprovementPercent")) improvementPercent = root.get("maeImprovementPercent").asDouble();
                if (root.has("regressionDetected")) regressionDetected = root.get("regressionDetected").asBoolean();
                if (root.has("recommendationStatus")) recommendationStatus = root.get("recommendationStatus").asText();
            }
        } catch (Exception ignored) {}

        return new ModelCandidateDto(
                c.getId(),
                c.getPredictionType(),
                c.getModelType(),
                c.getVersionTag(),
                c.getDatasetSnapshotId(),
                c.getFeatureSchemaVersion(),
                c.getTrainingSampleCount(),
                c.getValidationSampleCount(),
                c.getTestSampleCount(),
                c.getArtifactHash(),
                c.getStatus(),
                c.isImmutable(),
                c.getCreatedAt(),
                c.getCreatedBy(),
                testMae,
                baselineMae,
                improvementPercent,
                regressionDetected,
                recommendationStatus
        );
    }
}
