package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.AdaptiveSelectionDecisionDto;
import com.aquaai.prediction.domain.model.AdaptiveSelectionDecision;
import com.aquaai.prediction.domain.model.AdaptiveSelectionResult;
import com.aquaai.prediction.domain.model.ContextPerformanceProfile;
import com.aquaai.prediction.domain.model.PredictionContext;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.AdaptiveSelectionDecisionRepository;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.service.AdaptiveSelectionEngine;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectAdaptiveMethodUseCase {

    private final PredictionDataProvider dataProvider;
    private final PredictionMethodVersionRepository versionRepository;
    private final GetContextPerformanceProfilesUseCase profilesUseCase;
    private final GetFieldPredictionContextUseCase fieldContextUseCase;
    private final AdaptiveSelectionDecisionRepository decisionRepository;
    private final ObjectMapper objectMapper;

    private final AdaptiveSelectionEngine selectionEngine = new AdaptiveSelectionEngine();

    @Transactional
    public AdaptiveSelectionResult selectForPrediction(UUID fieldId, PredictionType predictionType, UUID predictionId) {
        FieldMetadata field = dataProvider.getFieldMetadata(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + fieldId));

        PredictionContext context = fieldContextUseCase.resolveDomainContext(field);
        List<PredictionMethodVersion> versions = versionRepository.findByPredictionType(predictionType);
        Map<String, List<ContextPerformanceProfile>> versionProfiles = profilesUseCase.getProfilesByVersion(predictionType);

        AdaptiveSelectionResult result = selectionEngine.selectBestMethod(
                predictionType,
                context,
                versions,
                versionProfiles
        );

        // Audit persistence
        String contextSnapshotJson = toJson(context);
        String alternativesJson = toJson(result.alternativesConsidered());

        AdaptiveSelectionDecision decision = AdaptiveSelectionDecision.create(
                predictionId,
                result,
                contextSnapshotJson,
                alternativesJson
        );
        decisionRepository.save(decision);

        log.info("Adaptive selection completed for field {} [{}]: selected {} via {}",
                fieldId, predictionType, result.selectedVersionTag(), result.contextMatchLevel());

        return result;
    }

    @Transactional
    public AdaptiveSelectionDecisionDto execute(UUID fieldId, PredictionType predictionType) {
        FieldMetadata field = dataProvider.getFieldMetadata(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + fieldId));

        AdaptiveSelectionResult result = selectForPrediction(fieldId, predictionType, null);
        AdaptiveSelectionDecision decision = decisionRepository.findByFieldIdAndType(fieldId, predictionType, 1)
                .stream().findFirst().orElse(null);

        return AdaptiveSelectionDecisionDto.fromDomain(decision, field.name());
    }

    private String toJson(Object obj) {
        if (obj == null) return "{}";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize adaptive context/alternatives to JSON", e);
            return "{}";
        }
    }
}
