package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionMethodVersionDto;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPredictionMethodVersionsUseCase {

    private final PredictionMethodVersionRepository versionRepository;

    @Transactional(readOnly = true)
    public List<PredictionMethodVersionDto> execute(PredictionType type) {
        if (type != null) {
            return versionRepository.findByPredictionType(type).stream()
                    .map(PredictionMethodVersionDto::fromDomain)
                    .collect(Collectors.toList());
        }
        return versionRepository.findAll().stream()
                .map(PredictionMethodVersionDto::fromDomain)
                .collect(Collectors.toList());
    }
}
