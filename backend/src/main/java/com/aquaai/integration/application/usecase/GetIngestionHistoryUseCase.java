package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.IngestedProvenanceDto;
import com.aquaai.integration.domain.repository.IngestedProvenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetIngestionHistoryUseCase {

    private final IngestedProvenanceRepository provenanceRepository;

    @Transactional(readOnly = true)
    public List<IngestedProvenanceDto> execute(UUID dataSourceId, UUID fieldId, int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), 100);
        if (dataSourceId != null) {
            return provenanceRepository.findRecentByDataSourceId(dataSourceId, boundedLimit).stream()
                    .map(IngestedProvenanceDto::from).toList();
        }
        if (fieldId != null) {
            return provenanceRepository.findRecentByFieldId(fieldId, boundedLimit).stream()
                    .map(IngestedProvenanceDto::from).toList();
        }
        return provenanceRepository.findRecent(boundedLimit).stream()
                .map(IngestedProvenanceDto::from).toList();
    }
}
